package com.system.auth.service;

import com.system.auth.mapper.SequenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SequenceService {
    private final SequenceMapper sequenceMapper;

    @Transactional
    public Long selectTokenSequence(){
        sequenceMapper.INCREMENT_TOKEN_ID();
        return sequenceMapper.SELECT_NEXT_TOKEN_ID();
    }
}
