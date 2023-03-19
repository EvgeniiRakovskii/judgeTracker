package com.rakovsky.judgeTracker.service;

import com.rakovsky.judgeTracker.model.CourtCase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WebPageService {

    private final static Map<String,String> headers;

    // переписать, да и в константы вынести
    static {
        headers = new HashMap<>();
        headers.put("Accept","*/*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
    }

    public Document getCasePageWithDelay(CourtCase courtCase) throws Exception {
        Document commonPage = getWebPage(courtCase.getUrlForCase());
        String link = commonPage.getElementsContainingOwnText(courtCase.getCaseNumber()).attr("href");
        TimeUnit.SECONDS.sleep(60);
        return getWebPage(link);

    }

    public boolean havePdfAct(Document casePage) {
        return casePage.getElementsByClass("contentt").size() > 1;
    }

    private Document getWebPage(String url) throws IOException {
        return Jsoup.connect(url)
                .headers(headers)
                .timeout(30000).get();
    }
}
