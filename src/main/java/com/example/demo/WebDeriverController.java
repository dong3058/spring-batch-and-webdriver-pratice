package com.example.demo;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class WebDeriverController {
    /*
      @GetMapping("/getPageList")
    public String webDriverController2(){
        WebDriver webDriver=webDriverConfig.WebDriverCreater();
        webDriver = webDriverConfig.WebDriverCreater();
        webDriver.get(productListUrl+productListUrl2);
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        List<String> itemList=new ArrayList<>();
        boolean hasNextPage=true;
        Long currentPage=1L;
        try {
            while(hasNextPage) {

                wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));

                List<WebElement> productUls = webDriver.findElements(By.cssSelector("ul.cate_prd_list.gtm_cate_list li"));

                productUls.stream().forEach(x -> {
                    log.info("product id:{}", x.getAttribute("criteo-goods"));
                    itemList.add(x.getAttribute("criteo-goods"));

                });
                List<WebElement> pageList = webDriver.findElements(By.cssSelector(".pageing a:not([class])"));

                if(!pageList.isEmpty()){
                    pageList.stream().forEach(x -> {
                        log.info("data-page-no:{}", x.getAttribute("data-page-no"));
                    });
WebElement pageging = webDriver.findElement(By.cssSelector(".pageing .next"));
                    log.info("current page:{}",currentPage);
                    log.info("이동할 페이지 번호:{}",pageging.getAttribute("data-page-no"));
currentPage=Long.parseLong(pageging.getAttribute("data-page-no"));
        pageging.click();

//js.executeScript("arguments[0].click();", pageList.getLast());
//pageList.getLast().click();
//Thread.sleep(1500);
                }
                        else{
                        throw new RuntimeException("마지막 페이지 도달 종료 실행");
                }
                        // pageNum+=1;
                        //pageging.click();
                        }

                        }
                        catch (Exception e){
        log.info("error:{}",e.getMessage());
WebDriver finalWebDriver = webDriver;
WebDriverWait wait2 = new WebDriverWait(finalWebDriver, Duration.ofSeconds(20));
JavascriptExecutor js2= (JavascriptExecutor) finalWebDriver;



            itemList.stream().forEach(x->{
getInfoFromProduct(x, finalWebDriver,wait2,js2);});
        }


        return "ok";
        }


@GetMapping("/webDriver")
public String webDriverController(){
    WebDriver webDriver = null;


    try {
        webDriver = webDriverConfig.WebDriverCreater();
        webDriver.get("https://www.oliveyoung.co.kr/store/goods/getGoodsDetail.do?goodsNo=A000000243672&dispCatNo=100000100010015&trackingCd=Cat100000100010015_Small&t_page=%EC%B9%B4%ED%85%8C%EA%B3%A0%EB%A6%AC%EA%B4%80&t_click=%ED%81%AC%EB%A6%BC_%EC%A0%84%EC%B2%B4__%EC%83%81%ED%92%88%EC%83%81%EC%84%B8&t_number=1");

        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) webDriver;

        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
        //Thread.sleep(2000);

        // 아코디언 버튼 클릭
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[class*='accordion-btn']")
                )
        );
        button.click();
        // js.executeScript("arguments[0].click();", button);
        wait.until(ExpectedConditions.attributeToBe(button, "aria-expanded", "true"));

        // 아코디언 컨텐츠 안에서 테이블 찾기
        WebElement content = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div[class*='Accordion_content']")
                )
        );

        // 컨텐츠 안의 테이블 찾기
        WebElement tableArea = content.findElement(By.cssSelector("div[class*='table-area']"));
        WebElement table = tableArea.findElement(By.tagName("table"));

        // 테이블 제목
        try {
            WebElement caption = table.findElement(By.tagName("caption"));
            log.info("=== {} ===", caption.getText());
        } catch (NoSuchElementException e) {
            log.info("caption 없음");
        }

        // tbody의 모든 행 가져오기
        WebElement tbody = table.findElement(By.tagName("tbody"));
        List<WebElement> rows = tbody.findElements(By.tagName("tr"));

        List<Map<String, String>> tableData = new ArrayList<>();

        for (WebElement row : rows) {
            Map<String, String> rowData = new HashMap<>();

            // th (헤더) 가져오기
            List<WebElement> headers = row.findElements(By.tagName("th"));
            if (!headers.isEmpty()) {
                rowData.put("header", headers.get(0).getText());
            }

            // td (데이터) 가져오기
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (int i = 0; i < cells.size(); i++) {
                rowData.put("data" + (i + 1), cells.get(i).getText());
            }

            tableData.add(rowData);
        }

        // 결과 출력
        log.info("=== 파싱된 테이블 데이터 ===");
        for (int i = 0; i < tableData.size(); i++) {
            log.info("행 {}: {}", i + 1, tableData.get(i));
        }

    }catch (TimeoutException e) {
        // [개량 5] 에러 시 스크린샷이나 현재 URL 상태 로그 남기기
        log.info("에러내용:{}",e.getMessage());
        //  log.info("텍스트 전문:{}",webDriver.getPageSource());
        log.error("타임아웃 발생! 현재 URL: {}", webDriver.getCurrentUrl());
        // 필요 시: log.info("Page Source: {}", webDriver.getPageSource());
    }
    catch (Exception e) {
        log.error("팀 크롤링 중 에러 발생: {}", e.getMessage(), e);
    } finally {
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (Exception e) {
                log.error("WebDriver quit 실패", e);
            }
        }
        return "ok";
    }
}

private void getInfoFromProduct(String productId,WebDriver webDriver,WebDriverWait wait,JavascriptExecutor js){
    try {
        log.info("product Id:{}", productId.substring(0, 13));
        webDriver.get(oliveYoungUrl + productId.substring(0, 13) + oliveYoungUrl2);
        wait.until(driver -> js.executeScript("return document.readyState").equals("complete"));
        // 아코디언 버튼 클릭
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[class*='accordion-btn']")
                )
        );
        button.click();
        // js.executeScript("arguments[0].click();", button);
        wait.until(ExpectedConditions.attributeToBe(button, "aria-expanded", "true"));

        // 아코디언 컨텐츠 안에서 테이블 찾기
        WebElement content = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div[class*='Accordion_content']")
                )
        );

        // 컨텐츠 안의 테이블 찾기
        By tableSelector = By.cssSelector("div[class*='Accordion_content'] div[class*='table-area'] table");

        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(tableSelector));

        // 테이블 제목
        try {
            WebElement caption = table.findElement(By.tagName("caption"));
            log.info("=== {} ===", caption.getText());
        } catch (NoSuchElementException e) {
            log.info("caption 없음");
        }

        // tbody의 모든 행 가져오기
        WebElement tbody = table.findElement(By.tagName("tbody"));
        List<WebElement> rows = tbody.findElements(By.tagName("tr"));

        List<Map<String, String>> tableData = new ArrayList<>();

        for (WebElement row : rows) {
            Map<String, String> rowData = new HashMap<>();

            // th (헤더) 가져오기
            List<WebElement> headers = row.findElements(By.tagName("th"));
            if (!headers.isEmpty()) {
                rowData.put("header", headers.get(0).getText());
            }

            // td (데이터) 가져오기
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (int i = 0; i < cells.size(); i++) {
                rowData.put("data" + (i + 1), cells.get(i).getText());
            }

            tableData.add(rowData);
        }

        // 결과 출력
        log.info("=== 파싱된 테이블 데이터 ===");
        for (int i = 0; i < tableData.size(); i++) {
            log.info("행 {}: {}", i + 1, tableData.get(i));
        }


        Thread.sleep(2000);

    }
    catch (Exception e){
        log.info("error :{}",e.getMessage());
        log.info("current url:{}",webDriver.getCurrentUrl());
    }
}*/
}
