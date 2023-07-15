package com.rakovsky.judgeTracker.service;

import com.rakovsky.judgeTracker.model.CourtCase;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toCollection;

@Service
public class WebPageService {

    @Autowired
    private WepPageRequestService wepPageRequestService;

    public Document getCasePageWithDelay(CourtCase courtCase) throws Exception {
        Document commonPage = wepPageRequestService.getWebPage(courtCase.getUrlForCase());
        String link = commonPage.getElementsContainingOwnText(courtCase.getCaseNumber()).attr("href");
        return wepPageRequestService.getWebPage(link);

    }

    private boolean havePdfAct(Document casePage) {
        return casePage.getElementsByClass("contentt").size() > 1;
    }

    public String getTableInfo(Document casePage) {
        Elements elements = casePage.getElementsByAttributeValueStarting("id", "cont");
        String tableInfo;

        if (havePdfAct(casePage)) {
            tableInfo = elements.stream().filter(element -> element.getElementsByAttributeValueStarting("id", "cont_doc").isEmpty()).
                    collect(toCollection(Elements::new)).text();
        } else {
            tableInfo = elements.text();
        }

        return tableInfo.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").replaceAll("\\d", "");
    }

    public int getNumberOfColumn(Document casePage) {
        return casePage.getElementsByClass("tabs").get(0).childNodeSize();
    }
}
