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

- Денежные суммы хранятся и агрегируются как `long` в рублях.
- `negate = true` инвертирует результат условия на уровне `QueryEngine`.
- Кеширование запросов knowledge DB реализовано через Caffeine.