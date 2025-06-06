# ApiOrcestr
Пошаговое создание коллекции в Postman для тестирования
Шаг 1: Откройте Postman
Запустите Postman

В левом сайдбаре нажмите "Collections"

Нажмите "+" (Create a new Collection)

Шаг 2: Настройте коллекцию
Дайте имя коллекции: "Keycloak Integration"

Перейдите на вкладку "Variables"

Добавьте переменные:

Variable	Initial Value	Current Value
keycloak_host	localhost:9090	localhost:9090
app_host	localhost:8080	localhost:8080
client_id	webapp	webapp
client_secret	supersecret	supersecret
realm	myrealm	myrealm
admin_token		
Нажмите "Save"

Шаг 3: Добавьте первый запрос (получение токена)
Нажмите "Add a request" внутри коллекции

Назовите запрос: "Get Admin Token"

Настройки:

Метод: POST

URL: http://{{keycloak_host}}/realms/master/protocol/openid-connect/token

Headers:

Key: Content-Type

Value: application/x-www-form-urlencoded

Body → x-www-form-urlencoded:
grant_type: client_credentials
client_id: {{client_id}}
client_secret: {{client_secret}}

Во вкладке "Tests" добавьте:
// Сохраняем токен в переменную
const response = pm.response.json();
pm.collectionVariables.set("admin_token", response.access_token);

Шаг 4: Добавьте второй запрос (создание пользователя)
Нажмите "Add a request" внутри коллекции

Назовите запрос: "Create User"

Настройки:

Метод: POST

URL: http://{{app_host}}/api/users

Headers:

Key: Content-Type

Value: application/json

Key: Authorization

Value: Bearer {{admin_token}}

Body → raw → JSON:
{
"username": "test_user",
"email": "test@example.com",
"firstName": "John",
"lastName": "Doe",
"password": "P@ssw0rd123!",
"enabled": true
}

Во вкладке "Tests" добавьте:

// Проверка успешного создания
pm.test("User created", () => {
pm.response.to.have.status(201);
});
Шаг 5: Добавьте третий запрос (проверка пользователя)
Нажмите "Add a request" внутри коллекции

Назовите запрос: "Verify User in Keycloak"

Настройки:

Метод: GET

URL: http://{{keycloak_host}}/admin/realms/{{realm}}/users?username=test_user

Headers:

Key: Authorization

Value: Bearer {{admin_token}}

Во вкладке "Tests" добавьте:

javascript
// Проверка наличия пользователя
pm.test("User exists in Keycloak", () => {
const response = pm.response.json();
pm.expect(response.length).to.be.above(0);
pm.expect(response[0].username).to.eql("test_user");
});
Как использовать коллекцию:

Запустите все сервисы:


docker-compose up -d
./gradlew bootRun

В Postman:
Откройте коллекцию "Keycloak Integration"

Последовательно выполните запросы:

"Get Admin Token" - получите токен

"Create User" - создайте пользователя

"Verify User in Keycloak" - проверьте создание

Результаты:

В первом запросе должен прийти access_token

Во втором запросе статус 201 Created

В третьем запросе - данные созданного пользователя

Советы:
Всегда выполняйте запросы по порядку

Для повторного тестирования:

Измените username в запросе создания пользователя

Или удалите пользователя через Keycloak Admin Console

Если токен истек - перезапустите первый запрос

Теперь вы можете тестировать все сценарии работы с Keycloak через эту коллекцию Postman!