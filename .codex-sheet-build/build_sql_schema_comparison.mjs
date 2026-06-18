import fs from "node:fs/promises";
import path from "node:path";
import { Workbook, SpreadsheetFile } from "@oai/artifact-tool";

const beforePath = "/Users/ikyoungtae/Downloads/before.sql";
const afterPath = "/Users/ikyoungtae/Downloads/after.sql";
const outputDir = path.resolve("../outputs/sql_schema_migration_20260618");
const outputPath = path.join(outputDir, "before_to_after_schema_change_analysis.xlsx");

const [beforeSql, afterSql] = await Promise.all([
  fs.readFile(beforePath, "utf8"),
  fs.readFile(afterPath, "utf8"),
]);

function scanCreateTables(sql) {
  const tables = new Map();
  const re = /create\s+table\s+`?([A-Za-z0-9_]+)`?\s*\(/gi;
  let match;
  while ((match = re.exec(sql))) {
    const name = match[1].toUpperCase();
    const open = re.lastIndex - 1;
    let depth = 0;
    let quote = null;
    let close = -1;
    for (let i = open; i < sql.length; i++) {
      const ch = sql[i];
      const prev = sql[i - 1];
      if (quote) {
        if (ch === quote && prev !== "\\") quote = null;
        continue;
      }
      if (ch === "'" || ch === '"' || ch === "`") {
        quote = ch;
      } else if (ch === "(") {
        depth++;
      } else if (ch === ")") {
        depth--;
        if (depth === 0) {
          close = i;
          break;
        }
      }
    }
    if (close < 0) continue;
    const semi = sql.indexOf(";", close);
    const tail = sql.slice(close + 1, semi < 0 ? close + 1 : semi);
    const body = sql.slice(open + 1, close);
    const items = splitTopLevel(body);
    const columns = new Map();
    const tablePk = new Set();
    const constraints = [];
    for (const raw of items) {
      const item = raw.trim().replace(/\s+/g, " ");
      if (!item) continue;
      const pk = item.match(/^primary\s+key\s*\(([^)]+)\)/i);
      if (pk) {
        for (const col of parseColumnList(pk[1])) tablePk.add(col);
        constraints.push({ kind: "PK", name: `PK_${name}`, columns: parseColumnList(pk[1]).join(", "), reference: "", deleteRule: "" });
        continue;
      }
      const fk = item.match(/^(?:constraint\s+`?([^`\s]+)`?\s+)?foreign\s+key\s*\(([^)]+)\)\s+references\s+`?([^`\s(]+)`?\s*\(([^)]+)\)([\s\S]*)/i);
      if (fk) {
        constraints.push({ kind: "FK", name: fk[1] || "(unnamed)", columns: parseColumnList(fk[2]).join(", "), reference: `${fk[3].toUpperCase()}(${parseColumnList(fk[4]).join(", ")})`, deleteRule: extractDeleteRule(fk[5]) });
        continue;
      }
      if (/^(constraint|unique|key|check)\b/i.test(item)) {
        const uq = item.match(/^constraint\s+`?([^`\s]+)`?\s+unique\s*\(([^)]+)\)/i);
        if (uq) constraints.push({ kind: "UNIQUE", name: uq[1], columns: parseColumnList(uq[2]).join(", "), reference: "", deleteRule: "" });
        continue;
      }
      const cm = item.match(/^`?([A-Za-z0-9_]+)`?\s+([\s\S]+)$/);
      if (!cm) continue;
      const colName = cm[1].toUpperCase();
      const definition = cm[2].trim();
      const column = parseColumn(colName, definition);
      columns.set(colName, column);
      if (column.primaryKey) tablePk.add(colName);
    }
    for (const col of tablePk) {
      if (columns.has(col)) columns.get(col).primaryKey = true;
    }
    if (tablePk.size && !constraints.some((c) => c.kind === "PK")) {
      constraints.unshift({ kind: "PK", name: `PK_${name}`, columns: [...tablePk].join(", "), reference: "", deleteRule: "" });
    }
    const comment = (tail.match(/comment\s*=\s*'([^']*)'|comment\s+'([^']*)'/i) || []).slice(1).find(Boolean) || "";
    tables.set(name, { name, comment, columns, constraints, indexes: [], rawTail: tail.replace(/\s+/g, " ").trim() });
    re.lastIndex = semi > close ? semi + 1 : close + 1;
  }

  const indexRe = /create\s+(unique\s+)?index\s+`?([^`\s]+)`?\s+on\s+`?([^`\s(]+)`?\s*\(([^)]+)\)/gi;
  while ((match = indexRe.exec(sql))) {
    const table = tables.get(match[3].toUpperCase());
    if (table) table.indexes.push({ kind: match[1] ? "UNIQUE INDEX" : "INDEX", name: match[2], columns: parseColumnList(match[4]).join(", "), reference: "", deleteRule: "" });
  }

  const alterFkRe = /alter\s+table\s+`?([^`\s]+)`?\s+add\s+constraint\s+`?([^`\s]+)`?\s+foreign\s+key\s*\(([^)]+)\)\s+references\s+`?([^`\s(]+)`?\s*\(([^)]+)\)([\s\S]*?);/gi;
  while ((match = alterFkRe.exec(sql))) {
    const table = tables.get(match[1].toUpperCase());
    if (table) table.constraints.push({ kind: "FK", name: match[2], columns: parseColumnList(match[3]).join(", "), reference: `${match[4].toUpperCase()}(${parseColumnList(match[5]).join(", ")})`, deleteRule: extractDeleteRule(match[6]) });
  }
  return tables;
}

function splitTopLevel(text) {
  const out = [];
  let start = 0;
  let depth = 0;
  let quote = null;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    const prev = text[i - 1];
    if (quote) {
      if (ch === quote && prev !== "\\") quote = null;
      continue;
    }
    if (ch === "'" || ch === '"' || ch === "`") quote = ch;
    else if (ch === "(") depth++;
    else if (ch === ")") depth--;
    else if (ch === "," && depth === 0) {
      out.push(text.slice(start, i));
      start = i + 1;
    }
  }
  out.push(text.slice(start));
  return out;
}

function parseColumnList(value) {
  return value.split(",").map((v) => v.replace(/`/g, "").trim().toUpperCase());
}

function extractDeleteRule(value) {
  const m = value.match(/on\s+delete\s+(cascade|restrict|set\s+null|no\s+action)/i);
  return m ? m[1].toUpperCase().replace(/\s+/g, " ") : "";
}

function parseColumn(name, definition) {
  const comment = (definition.match(/\bcomment\s+'([^']*)'/i) || [])[1] || "";
  const defaultMatch = definition.match(/\bdefault\s+('(?:[^']|'')*'|[^\s,]+)/i);
  const stop = definition.search(/\s+(?:primary\s+key|not\s+null|null|default|auto_increment|comment|collate|check)\b/i);
  const type = (stop < 0 ? definition : definition.slice(0, stop)).trim().toUpperCase().replace(/\s+/g, " ");
  return {
    name,
    type,
    nullable: !/\bnot\s+null\b/i.test(definition),
    defaultValue: defaultMatch ? defaultMatch[1].toUpperCase() : "",
    primaryKey: /\bprimary\s+key\b/i.test(definition),
    autoIncrement: /\bauto_increment\b/i.test(definition),
    comment,
    definition: definition.replace(/\s+/g, " "),
  };
}

const before = scanCreateTables(beforeSql);
const after = scanCreateTables(afterSql);

const tableAliases = {
  SYS_BBS_BRD: "SYS_BBS_PST",
  SYS_BBS_RPLY: "SYS_BBS_CMNT",
  SYS_EVT_LOG: "SYS_ADT_LOG",
  SYS_OFFC: "SYS_INST",
};

const columnAliases = {
  SYS_ACS_GRP_MENU: { ACS_RTS_GRP_ID: "GRP_ID", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_ACS_LOG: { EMAIL: "UP_GRP_ID", SYS_LOGIN_DT: "SYS_LGN_DT", SYS_LOGOUT_DT: "SYS_LGT_DT", DEV_TYPE: "ISTR_TYPE" },
  SYS_BBS: { BBS_MANAGER: "BBS_MNGR", FILE_YN: "FILE_ATCH_YN", REPLY_YN: "CMNT_YN", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", SYS_UPD_DT: "SYS_MDFCN_DT", P_MENU_CD: "UP_MENU_CD" },
  SYS_BBS_BRD: { BOARD_ID: "PST_ID", TITLE: "TTL", CONTENT: "CN", HIT_CNT: "INQ_CNT", THMBNL: "THMB_PATH", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", SYS_UPD_DT: "SYS_MDFCN_DT" },
  SYS_BBS_RPLY: { REPLY_ID: "CMNT_ID", BOARD_ID: "PST_ID", PARENT_REPLY_ID: "PRNT_CMNT_ID", CONTENT: "CN", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", SYS_UPD_DT: "SYS_MDFCN_DT" },
  SYS_BBS_MST: { SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", SYS_UPD_DT: "SYS_MDFCN_DT" },
  SYS_CD_GRP: { TOP_GRP_ID: "UP_GRP_ID", COMMENT: "EXPLN", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_CD: { SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "TUN_APRV_YMD" },
  SYS_DEPT_GRP: { TOP_GRP_ID: "UP_GRP_ID", SYS_CRT_DT: "SYS_GEN_DT", SYS_CRT_USR_ID: "SYS_GEN_ID", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_EVT_LOG: { TGT_TBL: "TRGT_TBL_NM", TGT_ID: "TRGT_ID", BFR_DATA: "CHG_PREV_DATA", AFT_DATA: "CHG_AFTR_DATA", EMAIL: "UP_GRP_ID" },
  SYS_FILE: { SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", TEMP_YN: "TMPR_FILE_YN" },
  SYS_FILE_DTL: { FILE_EXT: "FEXT", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_MENU: { MENU_SEQ: "MENU_UNQ_NO", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_OFFC: { TOP_OFFC_CD: "UP_INST_CD", OFFC_NM: "INST_NM", OFFC_CD: "INST_CD", OFFC_TYPE: "INST_SE_CD", OFFC_TYPE_CD: "INST_TYPE_CD", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", SYS_UPD_DT: "SYS_MDFCN_DT" },
  SYS_SITE_CFG: { SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_SITE_CFG_GRP: { SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
  SYS_TOKEN: { EXP: "EXPRY_YN", REVOK: "DSCD_YN", TOKEN: "TOKEN_VL" },
  SYS_TOKEN_SEQ: { NEXT_NOT_CACHED_VALUE: "NXT_NOT_CHCD_VAL", MINIMUM_VALUE: "MIN_VL", MAXIMUM_VALUE: "MAX_VL", START_VALUE: "BGNG_VL", INCREMENT: "INCRS_VL", CACHE_SIZE: "CACHE_SZ", CYCLE_OPTION: "CYCLE_OPT" },
  SYS_USER: { EMAIL: "EML", PWD: "PSWD", OFFC_CD: "INST_CD", USER_NM: "FNM", TEL: "TELNO", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID", SYS_UPD_DT: "SYS_MDFCN_DT" },
  SYS_USER_GRP: { USER_EMAIL: "USER_UP_GRP_ID", OFFC_CD: "INST_CD", SYS_CRT_DT: "SYS_GEN_DT", SYS_UPD_DT: "SYS_MDFCN_DT", SYS_UPD_USR_ID: "SYS_MDFR_ID" },
};

const confidenceOverrides = {
  "SYS_ACS_LOG.EMAIL": "낮음",
  "SYS_EVT_LOG.EMAIL": "낮음",
  "SYS_CD.SYS_UPD_USR_ID": "낮음",
  "SYS_USER_GRP.USER_EMAIL": "낮음",
  "SYS_OFFC.OFFC_TYPE": "중간",
  "SYS_USER.USER_NM": "중간",
};

function pairedAfterTable(beforeName) {
  if (after.has(beforeName)) return beforeName;
  return tableAliases[beforeName] || "";
}

const tablePairs = [];
const pairedAfterNames = new Set();
for (const beforeName of before.keys()) {
  const afterName = pairedAfterTable(beforeName);
  if (afterName && after.has(afterName)) {
    pairedAfterNames.add(afterName);
    tablePairs.push({ beforeName, afterName, status: beforeName === afterName ? "테이블 유지" : "테이블명 변경/대체" });
  } else {
    tablePairs.push({ beforeName, afterName: "", status: "삭제" });
  }
}
for (const afterName of after.keys()) {
  if (!pairedAfterNames.has(afterName)) tablePairs.push({ beforeName: "", afterName, status: "신규" });
}

function sameColumn(a, b) {
  return a.type === b.type && a.nullable === b.nullable && a.defaultValue === b.defaultValue && a.primaryKey === b.primaryKey && a.autoIncrement === b.autoIncrement;
}

function formatColumn(c) {
  if (!c) return "";
  return [c.type, c.nullable ? "NULL" : "NOT NULL", c.defaultValue ? `DEFAULT ${c.defaultValue}` : "", c.primaryKey ? "PK" : "", c.autoIncrement ? "AUTO_INCREMENT" : ""].filter(Boolean).join(" | ");
}

function differenceLabel(a, b) {
  const diffs = [];
  if (a.type !== b.type) diffs.push(`${a.type} → ${b.type}`);
  if (a.nullable !== b.nullable) diffs.push(`${a.nullable ? "NULL" : "NOT NULL"} → ${b.nullable ? "NULL" : "NOT NULL"}`);
  if (a.defaultValue !== b.defaultValue) diffs.push(`DEFAULT ${a.defaultValue || "없음"} → ${b.defaultValue || "없음"}`);
  if (a.primaryKey !== b.primaryKey) diffs.push(`PK ${a.primaryKey ? "Y" : "N"} → ${b.primaryKey ? "Y" : "N"}`);
  if (a.autoIncrement !== b.autoIncrement) diffs.push(`AUTO_INCREMENT ${a.autoIncrement ? "Y" : "N"} → ${b.autoIncrement ? "Y" : "N"}`);
  return diffs.join("; ");
}

function impactAndAction(changeType, beforeTable, afterTable, beforeCol, afterCol, diffs, confidence) {
  let impact = "낮음";
  let action = "코드 변경 불필요. 회귀 테스트만 수행";
  if (changeType === "컬럼 삭제") {
    impact = "높음";
    action = `데이터 보존 여부 결정 후 ${beforeTable}.${beforeCol} 참조(Entity/DTO/SQL/XML)을 제거 또는 대체`;
  } else if (changeType === "컬럼 추가") {
    impact = /NOT NULL/.test(diffs) ? "높음" : "중간";
    action = `${afterTable}.${afterCol} 필드를 Entity/DTO/입력·조회 SQL에 반영하고 기존 데이터 채움 정책 확인`;
  } else if (changeType.includes("이름변경")) {
    impact = confidence === "낮음" ? "높음" : "중간";
    action = `${beforeTable}.${beforeCol} 참조를 ${afterTable}.${afterCol}로 변경하고 SQL alias/직렬화 명칭 점검`;
  } else if (changeType === "정의 변경") {
    impact = /NOT NULL|PK|AUTO_INCREMENT|VARCHAR\([^)]*\) → VARCHAR\([^)]*\)|INT|CHAR/.test(diffs) ? "높음" : "중간";
    action = `${afterTable}.${afterCol}의 타입·NULL·기본값·키 정의에 맞춰 스키마와 검증 로직 수정`;
  }
  if (beforeTable && afterTable && beforeTable !== afterTable) {
    impact = impact === "낮음" ? "중간" : impact;
    action += `; 테이블 참조 ${beforeTable} → ${afterTable} 변경`;
  }
  return { impact, action };
}

const columnRows = [];
let sequence = 1;
for (const pair of tablePairs) {
  const bt = before.get(pair.beforeName);
  const at = after.get(pair.afterName);
  if (!bt && at) {
    for (const ac of at.columns.values()) {
      const diffs = formatColumn(ac);
      const { impact, action } = impactAndAction("컬럼 추가", "", pair.afterName, "", ac.name, diffs, "확실");
      columnRows.push([sequence++, "", pair.afterName, pair.status, "", ac.name, "컬럼 추가", "확실", "", formatColumn(ac), diffs, "", ac.comment, impact, action, "미착수", "", ""]);
    }
    continue;
  }
  if (bt && !at) {
    for (const bc of bt.columns.values()) {
      const { impact, action } = impactAndAction("컬럼 삭제", pair.beforeName, "", bc.name, "", formatColumn(bc), "확실");
      columnRows.push([sequence++, pair.beforeName, "", pair.status, bc.name, "", "컬럼 삭제", "확실", formatColumn(bc), "", formatColumn(bc), bc.comment, "", impact, action, "미착수", "", ""]);
    }
    continue;
  }
  const usedAfter = new Set();
  const aliases = columnAliases[pair.beforeName] || {};
  for (const bc of bt.columns.values()) {
    const targetName = at.columns.has(bc.name) ? bc.name : aliases[bc.name];
    const ac = targetName ? at.columns.get(targetName) : null;
    if (!ac) {
      const { impact, action } = impactAndAction("컬럼 삭제", pair.beforeName, pair.afterName, bc.name, "", formatColumn(bc), "확실");
      columnRows.push([sequence++, pair.beforeName, pair.afterName, pair.status, bc.name, "", "컬럼 삭제", "확실", formatColumn(bc), "", formatColumn(bc), bc.comment, "", impact, action, "미착수", "", ""]);
      continue;
    }
    usedAfter.add(ac.name);
    const renamed = bc.name !== ac.name;
    const changed = !sameColumn(bc, ac);
    const changeType = renamed && changed ? "이름+정의 변경" : renamed ? "이름변경" : changed ? "정의 변경" : "유지";
    const confidence = confidenceOverrides[`${pair.beforeName}.${bc.name}`] || (renamed ? "높음" : "확실");
    const diffs = differenceLabel(bc, ac) || (renamed ? `${bc.name} → ${ac.name}` : "동일");
    const { impact, action } = impactAndAction(changeType, pair.beforeName, pair.afterName, bc.name, ac.name, diffs, confidence);
    columnRows.push([sequence++, pair.beforeName, pair.afterName, pair.status, bc.name, ac.name, changeType, confidence, formatColumn(bc), formatColumn(ac), diffs, bc.comment, ac.comment, impact, action, changeType === "유지" ? "해당없음" : "미착수", "", confidence === "낮음" ? "업무 의미가 달라 보이므로 설계자 확인 필요" : ""]);
  }
  for (const ac of at.columns.values()) {
    if (usedAfter.has(ac.name)) continue;
    const diffs = formatColumn(ac);
    const { impact, action } = impactAndAction("컬럼 추가", pair.beforeName, pair.afterName, "", ac.name, diffs, "확실");
    columnRows.push([sequence++, pair.beforeName, pair.afterName, pair.status, "", ac.name, "컬럼 추가", "확실", "", formatColumn(ac), diffs, "", ac.comment, impact, action, "미착수", "", ""]);
  }
}

const tableRows = tablePairs.map((pair, i) => {
  const rows = columnRows.filter((r) => r[1] === pair.beforeName && r[2] === pair.afterName);
  const changed = rows.filter((r) => r[6] !== "유지").length;
  const high = rows.filter((r) => r[13] === "높음").length;
  const bt = before.get(pair.beforeName);
  const at = after.get(pair.afterName);
  let action = "변경 컬럼 기준으로 코드 수정";
  if (pair.status === "신규") action = "신규 테이블용 Entity/Mapper/Repository/API 필요 여부 확인";
  if (pair.status === "삭제") action = "기존 참조 제거 및 데이터 보존/이관 여부 결정";
  if (pair.status === "테이블명 변경/대체") action = `모든 참조를 ${pair.beforeName} → ${pair.afterName}로 전환`;
  return [i + 1, pair.beforeName, pair.afterName, pair.status, bt?.comment || "", at?.comment || "", bt?.columns.size || 0, at?.columns.size || 0, changed, high, action, high ? "P1" : changed ? "P2" : "P3", changed ? "미착수" : "해당없음", ""];
});

function constraintSignature(c) {
  return `${c.kind}|${c.columns}|${c.reference}|${c.deleteRule}`.toUpperCase();
}

const constraintRows = [];
let constraintSeq = 1;
for (const pair of tablePairs) {
  const bt = before.get(pair.beforeName);
  const at = after.get(pair.afterName);
  const beforeObjects = bt ? [...bt.constraints, ...bt.indexes] : [];
  const afterObjects = at ? [...at.constraints, ...at.indexes] : [];
  const used = new Set();
  for (const bc of beforeObjects) {
    const idx = afterObjects.findIndex((ac, j) => !used.has(j) && constraintSignature(ac) === constraintSignature(bc));
    if (idx >= 0) {
      used.add(idx);
      constraintRows.push([constraintSeq++, pair.beforeName, pair.afterName, bc.kind, bc.name, afterObjects[idx].name, "유지", bc.columns, afterObjects[idx].columns, bc.reference, afterObjects[idx].reference, bc.deleteRule, afterObjects[idx].deleteRule, "낮음", "변경 없음"]);
    } else {
      constraintRows.push([constraintSeq++, pair.beforeName, pair.afterName, bc.kind, bc.name, "", "삭제", bc.columns, "", bc.reference, "", bc.deleteRule, "", bc.kind.includes("INDEX") ? "중간" : "높음", "삭제 의도 확인 후 쿼리 성능·무결성 회귀 테스트"]);
    }
  }
  afterObjects.forEach((ac, idx) => {
    if (!used.has(idx)) constraintRows.push([constraintSeq++, pair.beforeName, pair.afterName, ac.kind, "", ac.name, "추가", "", ac.columns, "", ac.reference, "", ac.deleteRule, ac.kind.includes("INDEX") ? "중간" : "높음", "참조 컬럼 타입 및 기존 데이터 무결성 확인 후 적용"]);
  });
}

const warningRows = [
  ["P0", "문법", "SYS_ACS_LOG.ISTR_TYPE", "ENUM 값 목록 없이 ENUM만 선언됨", "MySQL CREATE TABLE 실행 실패 가능", "ENUM('PC','MOBILE','TABLET','OTHER') 등 허용값 확정"],
  ["P0", "문법", "SYS_ACHV_STNG.ID", "DEFAULT auto_increment 형태", "MySQL 문법 오류 가능", "BIGINT AUTO_INCREMENT PRIMARY KEY 형태로 수정"],
  ["P0", "외래키", "SYS_BBS_CMNT.PST_ID", "SYS_BBS_CMNT.CMNT_ID를 참조하는 자기참조 FK", "댓글의 게시물 연결이 잘못되고 FK 의미 불일치", "SYS_BBS_PST(PST_ID) 참조가 맞는지 확인"],
  ["P0", "외래키", "SYS_USER_GRP.GRP_ID", "SYS_USER(ID)를 참조하며 VARCHAR(32)→BIGINT 타입도 불일치", "FK 생성 실패 가능", "USER 식별 컬럼(ID 또는 USER_ID)으로 FK 재설계"],
  ["P0", "외래키", "SYS_ACHV_STNG.RGTR_ID", "VARCHAR(20)에서 SYS_USER.ID BIGINT 참조", "FK 생성 실패 가능", "타입을 맞추거나 USER_ID 참조로 변경"],
  ["P1", "기본키", "SYS_FILE_DTL / SYS_USER_GRP / SYS_CD / SYS_ACS_LOG", "컬럼별 PRIMARY KEY가 반복 선언됨", "복합키 의도라면 DDL 실행 또는 모델 해석 오류", "PRIMARY KEY(col1,col2) 테이블 제약조건으로 통합"],
  ["P1", "외래키", "SYS_BBS", "before의 SYS_BBS_MST FK가 after에서 사라짐", "게시판 마스터 무결성 약화", "삭제 의도 확인 후 FK 유지 여부 결정"],
  ["P1", "인덱스", "SYS_ACS_LOG", "파티션 및 3개 조회 인덱스가 after에서 사라짐", "로그 조회 성능과 보관 정책 영향", "파티션/인덱스 유지 여부를 운영 기준으로 결정"],
  ["P1", "제약조건", "SYS_TOKEN.TOKEN_VL", "before의 TOKEN UNIQUE와 USER FK가 after에서 사라짐", "중복 토큰 및 고아 토큰 가능", "UNIQUE/FK 삭제 의도 확인"],
  ["P1", "의미", "SYS_CD.TUN_APRV_YMD", "주석은 시스템수정자ID이나 컬럼명은 승인일자처럼 보이고 VARCHAR(20)", "잘못된 컬럼 매핑 가능", "SYS_MDFR_ID 누락 여부 및 실제 업무 의미 확인"],
];

const workbook = Workbook.create();
const summary = workbook.worksheets.add("00_요약");
const tableSheet = workbook.worksheets.add("01_테이블변경");
const columnSheet = workbook.worksheets.add("02_컬럼상세");
const constraintSheet = workbook.worksheets.add("03_키_인덱스_FK");
const warningSheet = workbook.worksheets.add("04_필수확인사항");
const checklistSheet = workbook.worksheets.add("05_작업체크리스트");

const colors = {
  navy: "#16324F", blue: "#1F6E8C", cyan: "#D8EEF3", pale: "#F4F8FA", line: "#CBD8DF",
  text: "#24343D", red: "#FCE2E0", redText: "#9C2F2A", orange: "#FCE8CF", yellow: "#FFF3C4",
  green: "#DCEFE4", gray: "#E9EEF1", white: "#FFFFFF",
};

function titleBand(sheet, title, subtitle, endCol) {
  sheet.showGridLines = false;
  sheet.mergeCells(`A1:${endCol}1`);
  sheet.getRange(`A1:${endCol}1`).values = [[title]];
  sheet.getRange(`A1:${endCol}1`).format = { fill: colors.navy, font: { bold: true, color: colors.white, size: 18 }, verticalAlignment: "center", rowHeight: 34 };
  sheet.mergeCells(`A2:${endCol}2`);
  sheet.getRange(`A2:${endCol}2`).values = [[subtitle]];
  sheet.getRange(`A2:${endCol}2`).format = { fill: colors.cyan, font: { color: colors.text, italic: true, size: 10 }, verticalAlignment: "center", rowHeight: 24 };
}

function writeTable(sheet, startRow, headers, rows, tableName, widths) {
  const start = startRow;
  const end = start + rows.length;
  const endCol = colName(headers.length);
  sheet.getRange(`A${start}:${endCol}${end}`).values = [headers, ...rows];
  const table = sheet.tables.add(`A${start}:${endCol}${end}`, true, tableName);
  table.style = "TableStyleMedium2";
  table.showBandedRows = true;
  table.showFilterButton = true;
  sheet.getRange(`A${start}:${endCol}${start}`).format = { fill: colors.blue, font: { bold: true, color: colors.white }, wrapText: true, verticalAlignment: "center", horizontalAlignment: "center", rowHeight: 34 };
  sheet.getRange(`A${start + 1}:${endCol}${end}`).format = { font: { color: colors.text, size: 9 }, verticalAlignment: "top", wrapText: true, borders: { preset: "all", style: "thin", color: colors.line } };
  widths.forEach((w, i) => sheet.getRange(`${colName(i + 1)}:${colName(i + 1)}`).format.columnWidth = w);
  sheet.freezePanes.freezeRows(start);
  return { start, end, endCol };
}

function colName(n) {
  let s = "";
  while (n) { n--; s = String.fromCharCode(65 + (n % 26)) + s; n = Math.floor(n / 26); }
  return s;
}

titleBand(summary, "Before → After DB 스키마 전환 분석", "현재 코드가 before 기준일 때 after로 변경하기 위한 영향 분석 및 작업 가이드 | 생성일 2026-06-18", "N");
summary.getRange("A4:N4").values = [["핵심 지표", "", "", "", "", "", "", "", "", "", "", "", "", ""]];
summary.mergeCells("A4:N4");
summary.getRange("A4:N4").format = { fill: colors.blue, font: { bold: true, color: colors.white, size: 12 }, rowHeight: 24 };

const kpis = [
  ["before 테이블", before.size, "after 테이블", after.size, "테이블 신규", tablePairs.filter((p) => p.status === "신규").length, "테이블 대체", tablePairs.filter((p) => p.status === "테이블명 변경/대체").length],
  ["변경 컬럼", columnRows.filter((r) => r[6] !== "유지").length, "고위험 컬럼", columnRows.filter((r) => r[13] === "높음").length, "필수 확인(P0)", warningRows.filter((r) => r[0] === "P0").length, "키/FK/인덱스 변경", constraintRows.filter((r) => r[6] !== "유지").length],
];
summary.getRange("A5:H6").values = kpis;
summary.getRange("B5").formulas = [[`=ROWS('01_테이블변경'!B5:B${4 + tableRows.length})-COUNTIF('01_테이블변경'!D5:D${4 + tableRows.length},"신규")`]];
summary.getRange("D5").formulas = [[`=ROWS('01_테이블변경'!C5:C${4 + tableRows.length})-COUNTIF('01_테이블변경'!D5:D${4 + tableRows.length},"삭제")`]];
summary.getRange("F5").formulas = [[`=COUNTIF('01_테이블변경'!D5:D${4 + tableRows.length},"신규")`]];
summary.getRange("H5").formulas = [[`=COUNTIF('01_테이블변경'!D5:D${4 + tableRows.length},"테이블명 변경/대체")`]];
summary.getRange("B6").formulas = [[`=ROWS('02_컬럼상세'!G5:G${4 + columnRows.length})-COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"유지")`]];
summary.getRange("D6").formulas = [[`=COUNTIF('02_컬럼상세'!N5:N${4 + columnRows.length},"높음")`]];
summary.getRange("F6").formulas = [[`=COUNTIF('04_필수확인사항'!A5:A${4 + warningRows.length},"P0")`]];
summary.getRange("H6").formulas = [[`=ROWS('03_키_인덱스_FK'!G5:G${4 + constraintRows.length})-COUNTIF('03_키_인덱스_FK'!G5:G${4 + constraintRows.length},"유지")`]];
for (const c of ["A", "C", "E", "G"]) summary.getRange(`${c}5:${c}6`).format = { fill: colors.gray, font: { bold: true, color: colors.text }, horizontalAlignment: "center", verticalAlignment: "center" };
for (const c of ["B", "D", "F", "H"]) summary.getRange(`${c}5:${c}6`).format = { fill: colors.pale, font: { bold: true, color: colors.blue, size: 15 }, horizontalAlignment: "center", verticalAlignment: "center" };
summary.getRange("A5:H6").format.borders = { preset: "all", style: "thin", color: colors.line };
summary.getRange("A5:H6").format.rowHeight = 30;
summary.getRange("A:A").format.columnWidth = 17; summary.getRange("B:B").format.columnWidth = 11;
summary.getRange("C:C").format.columnWidth = 17; summary.getRange("D:D").format.columnWidth = 11;
summary.getRange("E:E").format.columnWidth = 19; summary.getRange("F:F").format.columnWidth = 11;
summary.getRange("G:G").format.columnWidth = 20; summary.getRange("H:H").format.columnWidth = 11;

summary.getRange("A8:D14").values = [
  ["변경 유형", "건수", "색상 의미", "우선 대응"],
  ["컬럼 삭제", columnRows.filter((r) => r[6] === "컬럼 삭제").length, "빨강", "기존 참조/데이터 보존 확인"],
  ["컬럼 추가", columnRows.filter((r) => r[6] === "컬럼 추가").length, "파랑", "Entity·DTO·INSERT/UPDATE 반영"],
  ["이름변경", columnRows.filter((r) => r[6].includes("이름")).length, "주황", "전체 코드 검색 후 참조 변경"],
  ["정의 변경", columnRows.filter((r) => r[6] === "정의 변경").length, "노랑", "타입·NULL·기본값·키 반영"],
  ["유지", columnRows.filter((r) => r[6] === "유지").length, "회색", "회귀 테스트"],
  ["설계 확인", warningRows.length, "진한 빨강", "after.sql 적용 전 반드시 확정"],
];
summary.getRange("B9").formulas = [[`=COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"컬럼 삭제")`]];
summary.getRange("B10").formulas = [[`=COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"컬럼 추가")`]];
summary.getRange("B11").formulas = [[`=COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"이름변경")+COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"이름+정의 변경")`]];
summary.getRange("B12").formulas = [[`=COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"정의 변경")`]];
summary.getRange("B13").formulas = [[`=COUNTIF('02_컬럼상세'!G5:G${4 + columnRows.length},"유지")`]];
summary.getRange("B14").formulas = [[`=ROWS('04_필수확인사항'!A5:A${4 + warningRows.length})`]];
summary.getRange("A8:D8").format = { fill: colors.blue, font: { bold: true, color: colors.white }, horizontalAlignment: "center" };
summary.getRange("A8:D14").format.borders = { preset: "all", style: "thin", color: colors.line };
summary.getRange("A9:D14").format.wrapText = true;
summary.getRange("A9:D14").format.rowHeight = 24;
summary.getRange("C:C").format.columnWidth = 17; summary.getRange("D:D").format.columnWidth = 34;

summary.getRange("F8:N8").values = [["권장 전환 순서", "", "", "", "", "", "", "", ""]];
summary.mergeCells("F8:N8");
summary.getRange("F8:N8").format = { fill: colors.blue, font: { bold: true, color: colors.white } };
const steps = [
  "1. 04_필수확인사항의 P0 문법/FK 오류를 설계자와 먼저 확정",
  "2. 테이블 대체 4건의 데이터 이관 및 이름 변경 전략 수립",
  "3. PK·FK·UNIQUE·인덱스 정의를 확정하고 마이그레이션 DDL 작성",
  "4. 02_컬럼상세를 필터링해 Entity → DTO → Mapper/SQL → API 순서로 수정",
  "5. 기존 데이터 변환, NOT NULL/기본값 채움, 토큰·로그·게시판 회귀 테스트",
];
steps.forEach((s, i) => {
  summary.mergeCells(`F${9 + i}:N${9 + i}`);
  summary.getRange(`F${9 + i}:N${9 + i}`).values = [[s]];
});
summary.getRange("F9:N13").format = { fill: colors.pale, font: { color: colors.text }, wrapText: true, rowHeight: 26, borders: { preset: "all", style: "thin", color: colors.line } };

summary.getRange("A16:D22").values = [["차트 항목", "건수", "", ""], ...summary.getRange("A9:B14").values.map((r) => [r[0], r[1], "", ""])];
summary.getRange("B17:B22").formulas = [["=B9"], ["=B10"], ["=B11"], ["=B12"], ["=B13"], ["=B14"]];
const chart = summary.charts.add("bar", summary.getRange("A16:B22"));
chart.setPosition("F15", "N30");
chart.title = "변경 유형별 건수";
chart.hasLegend = false;
chart.xAxis = { axisType: "textAxis", textStyle: { fontSize: 9 } };
chart.yAxis = { numberFormatCode: "0" };
summary.getRange("A16:B22").format = { font: { size: 9 }, borders: { preset: "all", style: "thin", color: colors.line } };

titleBand(tableSheet, "테이블 단위 변경 요약", "테이블 추가·삭제·대체 여부와 변경 컬럼 수를 기준으로 우선순위를 확인하세요.", "N");
const tableRange = writeTable(tableSheet, 4, ["No", "Before 테이블", "After 테이블", "테이블 상태", "Before 설명", "After 설명", "Before 컬럼수", "After 컬럼수", "변경 컬럼수", "고위험수", "필요 작업", "우선순위", "상태", "비고"], tableRows, "TableChanges", [6, 22, 22, 20, 22, 22, 12, 12, 12, 10, 42, 10, 12, 28]);
tableSheet.getRange(`M5:M${tableRange.end}`).dataValidation = { rule: { type: "list", values: ["미착수", "진행중", "완료", "보류", "해당없음"] } };

titleBand(columnSheet, "컬럼별 상세 비교 및 코드 수정 가이드", "한 행이 하나의 before→after 컬럼 변경입니다. 변경 유형·영향도·신뢰도로 필터링해 작업하세요.", "R");
const columnRange = writeTable(columnSheet, 4, ["No", "Before 테이블", "After 테이블", "테이블 상태", "Before 컬럼", "After 컬럼", "변경 유형", "매핑 신뢰도", "Before 정의", "After 정의", "정의 차이", "Before 설명", "After 설명", "영향도", "필요 작업", "상태", "담당자", "비고"], columnRows, "ColumnChanges", [6, 20, 20, 18, 19, 19, 17, 12, 32, 32, 38, 24, 24, 10, 58, 12, 14, 34]);
columnSheet.getRange(`P5:P${columnRange.end}`).dataValidation = { rule: { type: "list", values: ["미착수", "진행중", "완료", "보류", "해당없음"] } };
columnSheet.getRange(`G5:G${columnRange.end}`).conditionalFormats.add("containsText", { text: "삭제", format: { fill: colors.red, font: { color: colors.redText, bold: true } } });
columnSheet.getRange(`G5:G${columnRange.end}`).conditionalFormats.add("containsText", { text: "추가", format: { fill: colors.cyan, font: { color: colors.blue, bold: true } } });
columnSheet.getRange(`G5:G${columnRange.end}`).conditionalFormats.add("containsText", { text: "이름", format: { fill: colors.orange, font: { color: "#8A4F08", bold: true } } });
columnSheet.getRange(`G5:G${columnRange.end}`).conditionalFormats.add("containsText", { text: "정의 변경", format: { fill: colors.yellow, font: { color: "#755B00", bold: true } } });
columnSheet.getRange(`N5:N${columnRange.end}`).conditionalFormats.add("containsText", { text: "높음", format: { fill: colors.red, font: { color: colors.redText, bold: true } } });

titleBand(constraintSheet, "기본키·외래키·인덱스 비교", "after에서 사라지거나 추가된 무결성/성능 요소입니다. 이름이 달라도 정의가 같으면 유지로 판정했습니다.", "O");
const constraintRange = writeTable(constraintSheet, 4, ["No", "Before 테이블", "After 테이블", "종류", "Before 이름", "After 이름", "상태", "Before 컬럼", "After 컬럼", "Before 참조", "After 참조", "Before DELETE", "After DELETE", "영향도", "확인 작업"], constraintRows, "ConstraintChanges", [6, 20, 20, 14, 28, 32, 10, 20, 20, 28, 28, 14, 14, 10, 46]);
constraintSheet.getRange(`G5:G${constraintRange.end}`).conditionalFormats.add("containsText", { text: "삭제", format: { fill: colors.red, font: { color: colors.redText, bold: true } } });
constraintSheet.getRange(`G5:G${constraintRange.end}`).conditionalFormats.add("containsText", { text: "추가", format: { fill: colors.cyan, font: { color: colors.blue, bold: true } } });

titleBand(warningSheet, "After.sql 적용 전 필수 확인사항", "문법 오류 가능성, FK 타입 불일치, 설계상 의심 항목입니다. P0는 DDL/코드 작업 전에 반드시 확정하세요.", "F");
const warningRange = writeTable(warningSheet, 4, ["우선순위", "분류", "대상", "발견 내용", "예상 영향", "확인/수정 제안"], warningRows, "Warnings", [12, 14, 31, 56, 50, 58]);
warningSheet.getRange(`A5:A${warningRange.end}`).conditionalFormats.add("containsText", { text: "P0", format: { fill: "#C9362B", font: { color: colors.white, bold: true } } });
warningSheet.getRange(`A5:A${warningRange.end}`).conditionalFormats.add("containsText", { text: "P1", format: { fill: colors.orange, font: { color: "#8A4F08", bold: true } } });

const actionableRows = columnRows.filter((r) => r[6] !== "유지").map((r, i) => [i + 1, r[13] === "높음" ? "P1" : "P2", r[1], r[2], r[4], r[5], r[6], r[14], r[15], r[16], "", r[17]]);
titleBand(checklistSheet, "실행용 마이그레이션 체크리스트", "변경이 필요한 컬럼만 모았습니다. 담당자·상태·완료일을 직접 관리할 수 있습니다.", "L");
const checklistRange = writeTable(checklistSheet, 4, ["No", "우선순위", "Before 테이블", "After 테이블", "Before 컬럼", "After 컬럼", "변경 유형", "작업 내용", "상태", "담당자", "완료일", "비고"], actionableRows, "MigrationChecklist", [6, 10, 20, 20, 19, 19, 17, 62, 12, 14, 14, 34]);
checklistSheet.getRange(`I5:I${checklistRange.end}`).dataValidation = { rule: { type: "list", values: ["미착수", "진행중", "완료", "보류"] } };
checklistSheet.getRange(`K5:K${checklistRange.end}`).setNumberFormat("yyyy-mm-dd");
checklistSheet.getRange(`B5:B${checklistRange.end}`).conditionalFormats.add("containsText", { text: "P1", format: { fill: colors.red, font: { color: colors.redText, bold: true } } });
checklistSheet.getRange(`I5:I${checklistRange.end}`).conditionalFormats.add("containsText", { text: "완료", format: { fill: colors.green, font: { color: "#22613C", bold: true } } });

for (const sheet of [tableSheet, columnSheet, constraintSheet, warningSheet, checklistSheet]) {
  const used = sheet.getUsedRange();
  used.format.verticalAlignment = "top";
}

await fs.mkdir(outputDir, { recursive: true });
const xlsx = await SpreadsheetFile.exportXlsx(workbook);
await xlsx.save(outputPath);

for (const sheetName of ["00_요약", "01_테이블변경", "02_컬럼상세", "03_키_인덱스_FK", "04_필수확인사항", "05_작업체크리스트"]) {
  const blob = await workbook.render({ sheetName, autoCrop: "all", scale: sheetName === "02_컬럼상세" ? 0.7 : 1, format: "png" });
  await fs.writeFile(path.join(outputDir, `${sheetName}.png`), new Uint8Array(await blob.arrayBuffer()));
}

const inspect = await workbook.inspect({ kind: "table", range: "00_요약!A1:N22", include: "values,formulas", tableMaxRows: 22, tableMaxCols: 14 });
const errors = await workbook.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A", options: { useRegex: true, maxResults: 100 }, summary: "final formula error scan" });
console.log(JSON.stringify({ outputPath, beforeTables: before.size, afterTables: after.size, columnRows: columnRows.length, actionableRows: actionableRows.length, constraints: constraintRows.length, warnings: warningRows.length, inspect: inspect.ndjson, errors: errors.ndjson }, null, 2));
