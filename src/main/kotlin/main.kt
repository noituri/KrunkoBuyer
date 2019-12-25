package noituri.krunkobuyer

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.Select
import java.util.*

fun WebElement.toPrice(): Int {
    return text
        .replace(" KR", "")
        .replace(",", "")
        .trim()
        .toInt()
}

fun ChromeDriver.krunkerLogin() {
    val account = String(Base64.getDecoder().decode(System.getenv("account")), Charsets.UTF_8).split(":")

    get("https://krunker.io")
    Thread.sleep(5000)
    findElementById("menuBtnProfile").click()
    findElementById("accName").sendKeys(account[0]) // kondor1939
    findElementById("accPass").sendKeys(account[1])
    executeScript("loginAcc()")
    Thread.sleep(1000)
}

fun ChromeDriver.searchAndBuy(keyword: String) {
    get("https://krunker.io/social.html?p=market")
    Thread.sleep(2000)
    findElementById("marketSearchB").click()
    val sortSelect = Select(findElementById("sortFilt"))
    sortSelect.selectByVisibleText("Lowest Price")
    findElementById("marketFiltTxt").sendKeys(keyword)
    findElementByXPath("//*[@id=\"filterPop\"]/div[11]/label/span").click()
    findElementByXPath("//*[@id=\"filterPop\"]/div[10]/label/span").click()
    findElementById("filterRBtn").click()
    Thread.sleep(1000)

    try {
        if (findElementByXPath("//*[@id=\"popupContent\"]/div/div[2]").text == "Error") krunkerLogin()
    } catch (e: Exception) {
        println("User is logged in!")
    }

    val item = findElementByClassName("marketCard")
    val itemPrice = item.findElement(By.className("marketPrice")).toPrice()

//    if (itemPrice > 20) return
    val money = findElementById("profileKR").toPrice()
    println("Searched for: $keyword> Price: $itemPrice KR | Your money: $money KR")
    if (itemPrice > money /*|| money < 250 */) {
        println("Not enough money!")
        return
    }

    val itemPopup = item.findElement(By.className("purchBtn")).getAttribute("onclick")
    executeScript(itemPopup)

    try {
        while (true) {
            findElementById("graceTimer")
        }
    } catch (e: Exception) {
        Thread.sleep(100)
        val buyScript = findElementByClassName("pItemButton").getAttribute("onclick")
        executeScript(buyScript)
        findElementById("popupHolder").click()
        println("Bought!")
    }
}

fun main() {
    var driver = ChromeDriver()
    driver.krunkerLogin()

    while (true) {
        Thread.sleep(2000)
        try {
            driver.searchAndBuy("knife")
        } catch (e: Exception) {
            driver.close()
            driver = ChromeDriver()
            driver.krunkerLogin()
        }
    }
}