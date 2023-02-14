package com.rakovsky.judgeTracker;

import com.rakovsky.judgeTracker.model.CourtCase;
import com.rakovsky.judgeTracker.service.CourtService;
import com.rakovsky.judgeTracker.service.WebPageService;
import com.rakovsky.judgeTracker.service.parser.ExcelParser;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;


@EnableJpaRepositories("com.rakovsky.judgeTracker.repository")
@EntityScan("com.rakovsky.judgeTracker.model")
@SpringBootApplication
public class JudgeTrackerApplication implements CommandLineRunner {

	@Autowired
	private CourtService courtService;
	@Autowired
	private ExcelParser excelParser;
	@Autowired
	private WebPageService webPageService;

	private final static String futureTable = "<tr>\n" +
			" <th colspan=\"10\">ДВИЖЕНИЕ ДЕЛА</th>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td align=\"center\"><b>Наименование события</b></td>\n" +
			" <td align=\"center\"><b>Дата</b></td>\n" +
			" <td align=\"center\"><b>Время</b></td>\n" +
			" <td align=\"center\"><b>Место проведения</b></td>\n" +
			" <td align=\"center\"><b>Результат события</b></td>\n" +
			" <td align=\"center\"><b>Основание для выбранного результата события</b></td>\n" +
			" <td align=\"center\"><b>Примечание</b></td>\n" +
			" <td align=\"center\"><b>Дата размещения</b>&nbsp;<span class=\"tooltipShow\"><img src=\"/images/help.gif\"><span>Информация о размещении событий в движении дела предоставляется на основе сведений, хранящихся в учетной системе судебного делопроизводства</span></span></td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Регистрация иска (заявления, жалобы) в суде</td>\n" +
			" <td>07.10.2022</td>\n" +
			" <td>16:18</td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td>07.10.2022</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Передача материалов судье</td>\n" +
			" <td>10.10.2022</td>\n" +
			" <td>13:06</td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td>10.10.2022</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Решение вопроса о принятии иска (заявления, жалобы) к рассмотрению</td>\n" +
			" <td>14.10.2022</td>\n" +
			" <td>15:29</td>\n" +
			" <td></td>\n" +
			" <td>Иск (заявление, жалоба) принят к производству</td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td>18.10.2022</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Вынесено определение о подготовке дела к судебному разбирательству</td>\n" +
			" <td>14.10.2022</td>\n" +
			" <td>15:29</td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td>18.10.2022</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Вынесено определение о назначении предварительного судебного заседания</td>\n" +
			" <td>14.10.2022</td>\n" +
			" <td>15:29</td>\n" +
			" <td>Тест на разницу текстов</td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td>18.10.2022</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Предварительное судебное заседание</td>\n" +
			" <td>14.11.2022</td>\n" +
			" <td>14:40</td>\n" +
			" <td>Зал 106</td>\n" +
			" <td>Назначено судебное заседание</td>\n" +
			" <td></td>\n" +
			" <td></td>\n" +
			" <td>18.10.2022</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			" <td>Судебное заседание</td>\n" +
			" <td>01.02.2023</td>\n" +
			" <td>16:00</td>\n" +
			" <td>Зал 106</td>\n" +
			" <td>Вынесено решение по делу</td>\n" +
			" <td>Иск (заявление, жалоба) УДОВЛЕТВОРЕН ЧАСТИЧНО</td>\n" +
			" <td></td>\n" +
			" <td>14.11.2022</td>\n" +
			"</tr>\n";


	@Override
	public void run(String... args) throws IOException {

		//List<CourtCase> newCases = excelParser.getCourtCasesFromExcel("C:\\Users\\RayS\\IdeaProjects\\judgeTracker\\judgeTracker\\cases.xlsx");
		//courtService.saveCases(newCases);

		List<CourtCase> cases = courtService.getAllCases().stream().sorted().toList();
		Set<String> differences = new HashSet<>();

		for (CourtCase courtCase:cases) {
			try {

				Document casePage = webPageService.getCasePageWithDelay(courtCase);

				int numberOfColumns = casePage.getElementsByClass("tabs").get(0).childNodeSize();

				// попробовать сохранить в бд?
				String tableInfo = casePage.getElementsContainingOwnText("Движение дела").get(1).parent().parent().html();
				System.out.println(courtCase);
				// разница в таблицах
				if (courtCase.getNumberOfColumn() != null && StringUtils.hasText(courtCase.getMotionOfCase())) {

					if (courtCase.getNumberOfColumn().equals(numberOfColumns)
							&& courtCase.getMotionOfCase().equals(tableInfo)) {
						continue;
					}
					DiffMatchPatch dmp = new DiffMatchPatch();
					DiffMatchPatch.Diff diff = dmp.diffMain(tableInfo, futureTable, false).stream().filter(difference -> difference.operation.equals(DiffMatchPatch.Operation.INSERT)).findFirst().orElseGet(null);
					differences.add("Разница у " + courtCase.getCustomName() + " в " + diff.text);

					// отправляем боту
					// бот спрашивает удалить ли ему дело? Если да - то дергается удаление,
					// надо наверное удалять по номеру дела
					// а дальше что? После принятия сохраняем в бд?
				} else {
					// заполняем таблицу значениями если информации нет
					courtCase.setNumberOfColumn(numberOfColumns);
					courtCase.setMotionOfCase(tableInfo);
					courtService.saveCourtCase(courtCase);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Ошибка " + courtCase);

			}
		}
		differences.forEach(System.out::println);
	}

	//Функции бота
	// Добавить дело, введите custom_name, введите url, введите номер дела
	// Загрузить excel, где бот говорит что excel должно быть следующего вида
	// Найти дело -> Удалить дело по номеру дела
	// Настроить время оповещений
	// Надо сделать белый лист, в котором будет указаны пользователи которые могут пользоваться ботом, если хотите пользоваться - напишите мне
	// написать тест
	public static void main(String[] args){

		SpringApplication.run(JudgeTrackerApplication.class, args);

	}


}
