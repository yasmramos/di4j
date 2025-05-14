
import com.univsoftdev.di4j.AppConfig;
import com.univsoftdev.di4j.Configuration;
import com.univsoftdev.di4j.Injector;

public class Main {
    
    public static void main(String[] args) {
        String propertiesFile = "application.properties";
        
        Injector injector = new Injector(new Configuration(), propertiesFile);
        injector.register(AppConfig.class);
        
        AppConfig resolve = injector.resolve(AppConfig.class);
        System.out.println(resolve.getAppName());
    }
}
