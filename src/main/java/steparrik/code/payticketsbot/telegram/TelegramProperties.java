package steparrik.code.payticketsbot.telegram;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("telegram-bot")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
public class TelegramProperties {
    String username;
    String token;
    String providerToken;
}
