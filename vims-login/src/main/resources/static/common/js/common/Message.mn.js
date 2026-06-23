/**
 * @title : Үндсэн CRUD MESSAGE-ийг жишиг болгон ашиглаж, хуудас бүрийн онцлогт тохируулан бичиж ашиглана.
 * @see : COMPLETE : Үйлдэл амжилттай дууссан
 * FAIL : Үйлдэл амжилтын баталгааг өгөх боломжгүй
 * CONFIRM : Асуулт хэлбэрээр дуусгах илэрхийлэл.
 * CHECK : Тодорхой утга байхгүй үед шаардах
 * INFO : Мэдээллийн чанартай мессеж
 * */
// Үндсэн CRUD MESSAGE
const Message = {};
Message.Label = {};
Message.Label.Array = {};


Message.Label.Array["CHECK.ID"] = "ID оруулна уу";
Message.Label.Array["CHECK.PWD"] = "Нууц үг оруулна уу";

Message.Label.Array["COMPLETE.INSERT"] = "Бүртгэл амжилттай";
Message.Label.Array["COMPLETE.READ"] = "Хайлт амжилттай";
Message.Label.Array["COMPLETE.UPDATE"] = "Засалт амжилттай";
Message.Label.Array["COMPLETE.DELETE"] = "Устгал амжилттай";
Message.Label.Array["COMPLETE.PERMIT"] = "Зөвшөөрөл амжилттай";
Message.Label.Array["COMPLETE.RETURN"] = "Буцаалт амжилттай";
Message.Label.Array["COMPLETE.RETURN_CANCEL"] = "Буцаалт цуцлалт амжилттай";
Message.Label.Array["COMPLETE.CANCEL"] = "Цуцлалт амжилттай";
Message.Label.Array["COMPLETE.SAVE"] = "Хадгалалт амжилттай";
Message.Label.Array["COMPLETE.RESEND"] = "Дахин илгээлт амжилттай";

Message.Label.Array["CONFIRM.INSERT"] = "Бүртгэх үү?";
Message.Label.Array["CONFIRM.PERMIT"] = "Зөвшөөрөх үү?";
Message.Label.Array["CONFIRM.DELETE"] = "Устгах уу?";
Message.Label.Array["CONFIRM.UPDATE"] = "Засах уу?";
Message.Label.Array["CONFIRM.RESET"] = "Шинэчлэх үү?";
Message.Label.Array["CONFIRM.CANCEL"] = "Цуцлах уу?";
Message.Label.Array["CONFIRM.RESEND"] = "Дахин илгээх үү?";
Message.Label.Array["CONFIRM.RESET"] = "Оруулсан бүх утга шинэчлэгдэнэ. Үргэлжлүүлэх үү?";
Message.Label.Array["CONFIRM.RESET_PWD"] = "Нууц үгийг шинэчлэх үү?";

Message.Label.Array["FAIL.INSERT"] = "Бүртгэл амжилтгүй";
Message.Label.Array["FAIL.SELECT"] = "Хайлт амжилтгүй";
Message.Label.Array["FAIL.SELECT_NO_DATA"] = "Өгөгдөл байхгүй байна";
Message.Label.Array["FAIL.UPDATE"] = "Засалт амжилтгүй";
Message.Label.Array["FAIL.DOWNLOAD"] = "Таталт амжилтгүй";
Message.Label.Array["FAIL.DELETE"] = "Устгал амжилтгүй";
Message.Label.Array["FAIL.PERMIT"] = "Зөвшөөрөл амжилтгүй";
Message.Label.Array["FAIL.RETURN"] = "Буцаалт амжилтгүй";
Message.Label.Array["FAIL.RETURN_CANCEL"] = "Буцаалт цуцлалт амжилтгүй";
Message.Label.Array["FAIL.LOGIN"] = "Нэвтрэлт амжилтгүй";
Message.Label.Array["FAIL.IDPWD"] = "ID эсвэл нууц үг буруу байна";
Message.Label.Array["FAIL.REQUIRED"] = "Заавал оруулах утгыг оруулна уу";
Message.Label.Array["FAIL.FUNCTION.POPUP"] = "Попап дуудах функц байхгүй байна";
Message.Label.Array["FAIL.NOT.EXIST.DISABLED"] = "data-disabled таг тохируулагдаагүй байна";
Message.Label.Array["FAIL.SAME.PWD"] = "Шинэ нууц үг хуучин нууц үгтэй ижил байна";
Message.Label.Array["FAIL.NOT.MATCHED.PWD"] = "Хуучин нууц үг таарахгүй байна";
Message.Label.Array["FAIL.COMPARE.PWD"] = "Нууц үг зөрүүтэй байна.";
Message.Label.Array["FAIL.UPLOAD"] = "Хуулах амжилтгүй";
Message.Label.Array["FAIL.UPLOAD.SIZE_EXCEEDED"] = "Файлын хэмжээ хэтэрсэн байна";
Message.Label.Array["FAIL.ACCESS_DENIED"] = "Хандах эрхгүй байна.";


Message.Label.Array["CHECK.FORMTYPE"] = "Хэлбэрт тохируулан оруулна уу";
Message.Label.Array["CHECK.DATA_ATTRIBUTE"] = "Тагийн шинж чанарыг дахин шалгана уу";
Message.Label.Array["CHECK.MATCH.PWD"] = "Хуучин нууц үгээ оруулна уу";
Message.Label.Array["CHECK.INPUT.PWD"] = "Нууц үг оруулна уу";
Message.Label.Array["CHECK.INPUT.PWD_CHECK"] = "Нууц үгийн баталгаажуулалтыг оруулна уу";

Message.Label.Array["BEFORE_PWD"] = "Хуучин нууц үг";

Message.Label.Array["ALERT.NO.CHANGED.FILE"] = "Өөрчлөгдсөн зүйл байхгүй байна";
Message.Label.Array["CONFIRM.CLOSE_UNSAVED_CHANGES"] = "Хадгалагдаагүй өөрчлөлт байна. Цонхыг хаах уу?";

Message.Label.Array["BACK_BTN"] = "Буцах";
Message.Label.Array["CLOSE_BTN"] = "Хаах";
Message.Label.Array["CANCEL_BTN"] = "Цуцлах";
Message.Label.Array["DELETE_BTN"] = "Устгах";
Message.Label.Array["DETAIL_BTN"] = "Дэлгэрэнгүй";
Message.Label.Array["UPDATE_BTN"] = "Засах";
Message.Label.Array["MORE_BTN"] = "Илүү";
Message.Label.Array["SAVE_BTN"] = "Хадгалах";
Message.Label.Array["SELECT_BTN"] = "Сонгох";
Message.Label.Array["SEARCH_BTN"] = "Хайх";
Message.Label.Array["SEARCH_KEYWORD"] = "Түлхүүр үг";
Message.Label.Array["REGISTER_BTN"] = "Бүртгэх";
Message.Label.Array["RESET_BTN"] = "Шинэчлэх";
Message.Label.Array["UPLOAD_BTN"] = "Хуулах";
Message.Label.Array["SIDE_BTN"] = "Хажуу";
Message.Label.Array["RESET_PWD_BTN"] = "Нууц үг шинэчлэх";
Message.Label.Array["REDIRECT_BTN"] = "Шилжих";
Message.Label.Array["WRITE_BTN"] = "Бичих";
Message.Label.Array["CONFIRM_BTN"] = "Баталгаажуулах";


Message.Label.Array["TOTAL"] = "Нийт";
Message.Label.Array["CASE"] = "шүүхийн";
Message.Label.Array["ALL"] = "Бүх";
Message.Label.Array["GALLERY"] = "Цомог";
Message.Label.Array["NOTICE"] = "Мэдэгдэл";
Message.Label.Array["SEARCH_PLACEHOLDER"] = "Хайх үгээ оруулна уу";
Message.Label.Array["NO_TITLE"] = "Гарчиггүй";
Message.Label.Array["UNKNOWN"] = "Мэдэхгүй";
Message.Label.Array["NO"] = "№";

Message.Label.Array["START_DATE"] = "Эхлэх огноо";
Message.Label.Array["END_DATE"] = "Дуусах огноо";


Message.Label.Array["FAIL.DELETE.REASON.EXIST_CHILD_DATA"] = "Доод түвшний өгөгдөл байгаа тул устгах боломжгүй";
Message.Label.Array["MAXIMUM.UPLOAD.SIZE.100MB"] = "Файлын хэмжээ 100 MB-аас хэтрэхгүй байх ёстой";
Message.Label.Array["ALERT.NO.FILE"] = "Файл сонгогдоогүй байна";
Message.Label.Array["FILE_DOWN_BTN"] = "Хавсаргасан файл татах";
Message.Label.Array["FILE_ATTACH"] = "Файл хавсаргах";
Message.Label.Array["FILE_ATTACH_INFO"] = "Хавсаргасан файлын жагсаалтыг доороос харна уу.";
Message.Label.Array["FILE_ATTACH_AREA_NOT_EXIST_FILE"] = "Хавсаргасан файл байхгүй байна.";
Message.Label.Array["FILE_REGISTER_BTN"] = "Файл нэмэх";
Message.Label.Array["FILE_NM"] = "Файлын нэр";
Message.Label.Array["FILE_SZ"] = "Файлын хэмжээ";
Message.Label.Array["FILE_EXT"] = "Өргөтгөл";
Message.Label.Array["FILE_DESCRIPTION"] = "Тайлбар";

Message.Label.Array["USE_YN"] = "Ашиглах эсэх";
Message.Label.Array["NOT_USED"] = "Ашиглахгүй";
Message.Label.Array["USED"] = "Ашиглах";

Message.Label.Array["LOGIN"] = "Нэвтрэх";
Message.Label.Array["EMAIL"] = "И-мэйл";
Message.Label.Array["PWD"] = "Нууц үг";
Message.Label.Array["REMEMBER_ME"] = "И-мэйл хадгалах";

// LOGIN PAGE
Message.Label.Array["LOGIN.KEEP_LOGGED_IN"] = "Нэвтрэлтийг хадгалах";
Message.Label.Array["LOGIN.FORGOT_PWD"] = "Нууц үг сэргээх";
Message.Label.Array["LOGIN.RESET_PWD_TITLE"] = "Нууц үг шинэчлэх";
Message.Label.Array["LOGIN.SEND_VERIFICATION_CODE"] = "Баталгаажуулах код илгээх";
Message.Label.Array["LOGIN.VERIFICATION_CODE"] = "Баталгаажуулах код";
Message.Label.Array["LOGIN.CONFIRM_VERIFICATION_CODE"] = "Код шалгах";
Message.Label.Array["LOGIN.NEW_PWD"] = "Шинэ нууц үг";
Message.Label.Array["LOGIN.PWD_POLICY_GUIDE"] = "4-12 тэмдэгт, латин үсэг, тоо, тусгай тэмдэгт орсон байна";
Message.Label.Array["LOGIN.NEW_PWD_CONFIRM"] = "Шинэ нууц үг баталгаажуулах";
Message.Label.Array["LOGIN.PWD_CONFIRM"] = "Нууц үг баталгаажуулах";
Message.Label.Array["LOGIN.INITIALIZE_PWD_BTN"] = "Нууц үг шинэчлэх";
Message.Label.Array["LOGIN.APPLY_BTN"] = "Хэрэглэх";
Message.Label.Array["LOGIN.CLOSE_BTN"] = "Хаах";
Message.Label.Array["LOGIN.MAIL_TITLE_VERIFICATION"] = "[giens] Баталгаажуулах код илгээлээ";
Message.Label.Array["LOGIN.FAIL_SEND_VERIFICATION_CODE"] = "Баталгаажуулах код илгээх амжилтгүй";
Message.Label.Array["LOGIN.EXP_VERIFICATION_CODE"] = "Баталгаажуулах кодын хугацаа дууссан байна.";
Message.Label.Array["LOGIN.MAIL_CONTENT_HEADER"] = "[giens] И-мэйл баталгаажуулах код";
Message.Label.Array["LOGIN.MAIL_CONTENT_BODY1"] = "Нууц үг шинэчлэх баталгаажуулах кодыг илгээлээ.";
Message.Label.Array["LOGIN.MAIL_CONTENT_BODY2"] = "Доорх баталгаажуулах кодыг оруулна уу.";
Message.Label.Array["LOGIN.COMPLETE_REGISTER"] = "Бүртгэл амжилттай";
Message.Label.Array["LOGIN.INPUT_VERIFICATION_CODE"] = "Баталгаажуулах кодоо оруулна уу.";
Message.Label.Array["LOGIN.NEW_PWD"] = "Шинэ нууц үг";
Message.Label.Array["LOGIN.NEW_PWD_CONFIRM"] = "Шинэ нууц үг баталгаажуулах";
Message.Label.Array["LOGIN.RESET_PWD_PAGE_GUIDE"] = "Нууц үгээ дахин тохируулна уу.";
Message.Label.Array["LOGIN.RESET_PWD_INVALID_ACCESS"] = "Буруу хандалт. Нууц үг сэргээхийг дахин эхлүүлнэ үү.";
Message.Label.Array["CHECK.INPUT.EMAIL"] = "Имэйлээ оруулна уу";
