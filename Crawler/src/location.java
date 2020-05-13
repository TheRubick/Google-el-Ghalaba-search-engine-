import java.io.IOException;

import org.jsoup.Jsoup;

public class location {
     
    public static void main(String[] args) {
	try {
		String CountryDomain = Jsoup.connect("https://ipapi.co/country_code/").get().body().text();
		System.out.println(CountryDomain);
		//HINT : use lastIndexOf to compare between the website URL and the CountryDomain
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
     
    }
     
}