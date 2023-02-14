package com.rakovsky.judgeTracker;

import com.rakovsky.judgeTracker.bot.LawyerHelperBot;
import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.service.CourtService;
import com.rakovsky.judgeTracker.service.WebPageService;
import com.rakovsky.judgeTracker.service.parser.ExcelParser;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.*;


@EnableJpaRepositories("com.rakovsky.judgeTracker.repository")
@EntityScan("com.rakovsky.judgeTracker.model")
@SpringBootApplication
@EnableScheduling
//public class JudgeTrackerApplication implements CommandLineRunner {
public class JudgeTrackerApplication {

	@Autowired
	private CourtService courtService;
	@Autowired
	private ExcelParser excelParser;
	@Autowired
	private WebPageService webPageService;
	@Autowired
	private LawyerHelperBot lawyerHelperBot;

	private static final Logger logger = LoggerFactory.getLogger(JudgeTrackerApplication.class);


	/*
	@Override
	public void run(String... args) {

	}

	 */

	//Функции бота
	// Добавить дело, введите custom_name, введите url, введите номер дела
	// Загрузить excel, где бот говорит что excel должно быть следующего вида
	// Найти дело -> Удалить дело по номеру дела
	// Настроить время оповещений
	// Надо сделать белый лист, в котором будет указаны пользователи которые могут пользоваться ботом, если хотите пользоваться - напишите мне
	// написать тест
	public static void main(String[] args) {
		SpringApplication.run(JudgeTrackerApplication.class, args);

	}


}
