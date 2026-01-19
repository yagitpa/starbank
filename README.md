# StarBank Recommendation Service

Сервис рекомендаций банковских продуктов.

---

## Что реализовано

### Stage 1
- Эндпоинт получения рекомендаций: `GET /recommendation/{userId}`
- Фиксированные правила рекомендаций (rule sets)
- Основная БД знаний (users / products / transactions)
- Работа с основной БД через `JdbcTemplate`

### Stage 2
- Динамические правила рекомендаций (CRUD через REST API)
- Отдельная rules DB (JPA + Liquibase)
- Rule Engine для исполнения условий (4 типа query)
- Поддержка `negate` и операторов сравнения
- Кеширование SQL-запросов knowledge DB через Caffeine
- Интеграция динамических правил в `/recommendation/{userId}` без breaking changes
- OpenAPI / Swagger UI
- Unit и Integration tests

### Stage 3
- Статистика динамических правил
- Management API (очистка кэшей, информация о сервисе)
- Telegram Bot (PoC)

---

## Архитектура

### Базы данных

**knowledge DB (основная)**  
Содержит пользователей, продукты и транзакции. Используется для расчётов условий динамических правил и для фиксированных правил Stage 1. Доступ — через `JdbcTemplate`.

**rules DB (вторая)**  
Хранит динамические правила. Доступ — через JPA (`RuleRepository`). Схема создаётся Liquibase (`db/changelog/db.changelog-rules.yaml`).

### Правила

**Fixed rules (Stage 1)**  
Реализованы через `RecommendationRuleSet` и остаются без изменений.

**Dynamic rules (Stage 2)**  
Хранятся в rules DB, загружаются и исполняются через `QueryEngine` и набор executor-ов.

---

## API

### Swagger / OpenAPI
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

---

## Эндпоинты

### Получение рекомендаций
`GET /recommendation/{userId}`

Возвращает рекомендации, сформированные:
- фиксированными правилами (Stage 1)
- динамическими правилами (Stage 2)

Контракт API не изменён относительно Stage 1.

---

### Управление динамическими правилами

#### Создание правила
`POST /rule`

Пример:
```json
{
  "product_name": "Кредитная карта",
  "product_id": "11111111-1111-1111-1111-111111111111",
  "product_text": "Оформите кредитную карту с льготным периодом до 120 дней",
  "rule": [
    {
      "query": "USER_OF",
      "arguments": ["DEBIT"],
      "negate": false
    }
  ]
}
```

#### Получение всех правил
`GET /rule`

Ответ:
```json
{
  "data": [
    {
      "id": 1,
      "product_name": "Кредитная карта",
      "product_id": "11111111-1111-1111-1111-111111111111",
      "product_text": "Оформите кредитную карту с льготным периодом до 120 дней",
      "rule": [
        {
          "query": "USER_OF",
          "arguments": ["DEBIT"],
          "negate": false
        }
      ]
    }
  ]
}
```

#### Удаление правила
`DELETE /rule/{id}`

Возвращает `204 No Content`.

### Получить статистику по всем правилам 
`GET /rule/stats`

Поведение:
- Возвращает статистику **по всем** динамическим правилам, которые существуют в rules DB.
- Если по правилу ещё не было срабатываний, оно **всё равно присутствует** в списке со значением `count = 0`.
- `rule_id` — идентификатор правила (id из rules DB).
- `count` — количество срабатываний правила (инкрементируется каждый раз, когда правило выполнилось для пользователя и рекомендация была выдана).

Пример ответа:
```json
{
  "stats": [
    { "rule_id": "1", "count": "3" },
    { "rule_id": "2", "count": "0" }
  ]
}
```

### Очистка кешей 
`POST /management/clear-caches`

Поведение:
- Запрос **без тела** (ничего не принимается через `@RequestBody`).
- Очищаются **все** кеши рекомендаций (включая кеши репозитория knowledge DB).
- После вызова следующие запросы рекомендаций должны снова обращаться к БД (кеш прогреется заново).

Ответ (HTTP 200): пустое тело.

### Информация о сервисе 
`GET /management/info`

Поведение:
- Возвращает информацию о сервисе в формате:
  - `name` — название сервиса (artifactId)
  - `version` — версия из `pom.xml`
- Значения извлекаются динамически через build-info (Spring Boot BuildProperties).

Ответ (HTTP 200):
```json
{
  "name": "starbank-recommendation",
  "version": "1.0.0"
}
```

---

## Типы query (Stage 2)

### 1) USER_OF
Проверяет, что у пользователя есть хотя бы одна транзакция по продукту типа `productType`.

Формат:
```json
{ "query": "USER_OF", "arguments": ["DEBIT"], "negate": false }
```

---

### 2) ACTIVE_USER_OF
Активный пользователь продукта — количество транзакций по продукту >= 5.

Формат:
```json
{ "query": "ACTIVE_USER_OF", "arguments": ["DEBIT"], "negate": false }
```

---

### 3) TRANSACTION_SUM_COMPARE
Сравнивает сумму транзакций по продукту и типу транзакции с порогом.

Формат arguments:
```
[productType, transactionType, operator, amount]
```

Пример:
```json
{
  "query": "TRANSACTION_SUM_COMPARE",
  "arguments": ["DEBIT", "WITHDRAW", ">", "100000"],
  "negate": false
}
```

Поддерживаемые операторы:
```
>, <, =, >=, <=
```

---

### 4) TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW
Сравнивает суммы `DEPOSIT` и `WITHDRAW` по продукту.

Формат arguments:
```
[productType, operator]
```

Пример:
```json
{
  "query": "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
  "arguments": ["SAVING", ">="],
  "negate": false
}
```

---

## Запуск проекта

### Требования
- Java 17
- Maven

### Конфигурация БД

- `spring.datasource.*` — knowledge DB
- `spring.rules-datasource.*` — rules DB

Миграции rules DB выполняются через Liquibase:
`db/changelog/db.changelog-rules.yaml`.

### Telegram Bot (PoC)

Команды:
- `/start` — приветствие и справка
- `/recommend <username>` — получение рекомендаций

Поведение:
- найден ровно один пользователь → выдаются рекомендации
- 0 или >1 пользователей → "Пользователь не найден"

Конфигурация (без хранения токена в git):

```yaml
telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN:}
    username: ${TELEGRAM_BOT_USERNAME:}
```

---

## Тесты

### Unit tests
```bash
mvn test
```

### Integration tests (Stage 2)
Используется профиль `test`, обе БД поднимаются в H2 in-memory.
```bash
mvn test -Dspring.profiles.active=test
```

---

## Примечания

- Денежные суммы хранятся и агрегируются как `long` в рублях (Stage 1)
- `negate = true` инвертирует результат условия на уровне `QueryEngine` (Stage 2)
- Кеширование запросов knowledge DB реализовано через Caffeine (Stage 2)
- Рекомендации кешируются (Stage 3)
- Кеш очищается через Management API (Stage 3)