package com.song7749.common.service;

import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.song7749.base.MessageVo;
import com.song7749.common.value.MailConfigDto;
import com.song7749.common.value.MailConfigVo;
import com.song7749.mail.domain.MailConfig;
import com.song7749.mail.repository.MailConfigRepository;
import com.song7749.mail.service.EmailService;
import com.song7749.util.validate.Validate;

@Service
public class CommonConfigServiceImpl implements CommonConfigService{

	@Autowired
	MailConfigRepository mailConfigRepository;

	@Autowired
	EmailService emailService;

	@Autowired
	ModelMapper mapper;

	@Override
	public MessageVo testMailConfig(MailConfigDto dto) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(dto.getHost());
	    mailSender.setPort(dto.getPort());
	    mailSender.setProtocol(dto.getProtocol().toString());
	    mailSender.setUsername(dto.getUsername());
	    mailSender.setPassword(dto.getPassword());
	    // 추가 설정
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.smtp.auth", dto.getAuth());
        props.put("mail.smtp.EnableSSL.enable",dto.getEnableSSL());
	    props.put("mail.smtp.starttls.enable", dto.getStarttls());
//	    props.put("mail.smtp.connectiontimeout", "5000");
//	    props.put("mail.smtp.timeout", "3000");
//	    props.put("mail.smtp.writetimeout", "5000");
	    props.put("mail.debug", true);

	    MessageVo vo = new MessageVo();
	    try {
	    	mailSender.testConnection();
	    	vo.setHttpStatus(HttpStatus.OK.value());
	    	vo.setMessage("메일 연결 테스트 완료!");
		} catch (MessagingException e) {
	    	vo.setHttpStatus(HttpStatus.BAD_REQUEST.value());
	    	vo.setMessage(e.getMessage());
		}
		return vo;
	}

	@Validate
	@Transactional
	@Override
	public synchronized MailConfigVo saveMailConfig(MailConfigDto dto) {
		List<MailConfig> list = mailConfigRepository.findAll();
		MailConfig config = null;
		// 기존 설정이 없는 경우에
		if(CollectionUtils.isEmpty(list)) {
			config = mailConfigRepository.saveAndFlush(mapper.map(dto, MailConfig.class));
		} else { // 이미 설정이 있는 경우
			config = list.get(0);
			mapper.map(dto,config); // 변경된 값으로 저장
			mailConfigRepository.saveAndFlush(config);
		}
		// 메일 환경 리셋
		emailService.getMailSender(true);
		return mapper.map(config, MailConfigVo.class);
	}

	@Transactional(readOnly=true)
	@Override
	public MailConfigVo findMailConfig() {
		// TODO Auto-generated method stub
		List<MailConfig> list = mailConfigRepository.findAll();
		MailConfig config = null;
		if(!CollectionUtils.isEmpty(list)) {
			return  mapper.map(list.get(0), MailConfigVo.class);
		} else {
			return null;
		}
	}
}