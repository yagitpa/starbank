# StarBank Recommendation Service (Stage 1)

Сервис рекомендаций банковских продуктов на Java 17 + Spring Boot 3.5.x.

## Ключевые особенности

- **H2 file-based БД (read-only)**: приложение ничего не пишет в БД и не управляет схемой
- **Только JdbcTemplate** (по требованиям Stage 1)
- Денежные суммы в БД хранятся **в копейках**, сервис работает с суммами **в рублях** через `BigDecimal`
- Архитектура рекомендаций: `RecommendationRuleSet` + 3 реализации (`@Component`)
- OpenAPI/Swagger UI

## Технологии

- Java 17
- Spring Boot 3.5.9
- H2 Database
- Spring JDBC (JdbcTemplate)
- springdoc-openapi
- JUnit 5 + Mockito

## Как запустить

### 1) Требования
- JDK 17
- IntelliJ IDEA (или любая IDE)
- Maven (можно запускать через Maven tool window в IntelliJ)

### 2) Подготовка H2 файла базы
Положите файл базы рядом с `pom.xml`.

Пример для URL `jdbc:h2:file:./transaction`:
- файл должен называться `transaction.mv.db`

Если файл называется иначе, поменяйте URL в `application.yml` на соответствующий.

### 3) Настройки приложения
`src/main/resources/application.yml` содержит подключение в read-only режиме:

- `ACCESS_MODE_DATA=r`

Логин и пароль не используются (пустые значения).

### 4) Запуск
Через IntelliJ:
- Maven Tool Window → Lifecycle → `test`
- затем запустить `StarBankRecommendationApplication`

Или командой (если Maven есть в PATH):
```bash
mvn clean test
mvn spring-boot:run
