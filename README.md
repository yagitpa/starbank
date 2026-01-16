# StarBank Recommendation Service

Сервис рекомендаций банковских продуктов.

## Что реализовано

### Stage 1
- Эндпоинт получения рекомендаций: `GET /recommendation/{userId}`
- Фиксированные правила рекомендаций (rule sets)
- Основная БД знаний (transactions/products/users) — через `JdbcTemplate`

### Stage 2
- Динамические правила (CRUD через REST API) во второй БД (rules DB)
- Rule Engine для исполнения условий (4 типа query)
- Кеширование SQL-запросов к knowledge DB через Caffeine
- Интеграция динамических правил в `/recommendation/{userId}` без breaking changes
- OpenAPI / Swagger UI
- Unit + integration tests

---

## Архитектура (кратко)

- **knowledge DB** (основная): используется для расчётов по транзакциям (JdbcTemplate)
- **rules DB** (вторая): хранит динамические правила (JPA + Liquibase)
- **Fixed rules** (Stage 1): остаются как есть
- **Dynamic rules** (Stage 2): загружаются из rules DB и исполняются QueryEngine

---

## API

### Swagger / OpenAPI
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

---

### Получение рекомендаций
`GET /recommendation/{userId}`

Возвращает рекомендации по фиксированным и динамическим правилам.

---

### Управление динамическими правилами

#### Создать правило
`POST /rule`

Тело запроса:
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