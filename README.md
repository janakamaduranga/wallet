# wallet

Assumptions
1) Transactions are performed under the same currency
2) Authentication is out of scope

Please note below information
1) swagger url http://localhost:8091/wallet/swagger-ui/index.html
2) H2 db console http://localhost:8091/wallet/h2-console
  a) db password is "password"
3) need to pass vm arguments "-Dspring.profiles.active=dev -Djasypt.encryptor.password=wallet"
4) dummy Authorization Header must be passed in Header

How to build and run
1) command to build : mvn clean install
2) command to start the application: mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev -Djasypt.encryptor.password=wallet"

How to test the application

You can use swagger UI or curl command to test the application. Swagger UI url is provided above
Note: in swagger UI, make sure you click "lock" icon and pass dummy jwt token(any string works)
1) Submit a credit transaction
   curl -X 'POST' \
   'http://localhost:8091/wallet/v1/accounts/user123/transactions/credits' \
   -H 'accept: */*' \
   -H 'Authorization: Bearer cxvdf33' \
   -H 'Content-Type: application/json' \
   -d '{
   "id": "212133",
   "userTransactionTime": "2022-10-30T08:18:31.775Z",
   "amount": 2345.00
   }'
2) Submit a debit transaction
   curl -X 'POST' \
   'http://localhost:8091/wallet/v1/accounts/user123/transactions/debits' \
   -H 'accept: */*' \
   -H 'Authorization: Bearer cxvdf33' \
   -H 'Content-Type: application/json' \
   -d '{
   "id": "3223423",
   "userTransactionTime": "2022-10-30T08:15:54.046Z",
   "amount": 234.00
   }'
3) Get transaction for a given account id
   curl -X 'GET' \
   'http://localhost:8091/wallet/v1/accounts/user123/transactions?page=0&size=10&sort=id' \
   -H 'accept: */*' \
   -H 'Authorization: Bearer cxvdf33'
4) Create user account
   curl -X 'POST' \
   'http://localhost:8091/wallet/v1/accounts' \
   -H 'accept: */*' \
   -H 'Authorization: Bearer tt' \
   -H 'Content-Type: application/json' \
   -d '{
   "accountId": "test999",
   "balance": 2345.00
   }'
5) Get by account Id
   curl -X 'GET' \
   'http://localhost:8091/wallet/v1/accounts/test999' \
   -H 'accept: */*' \
   -H 'Authorization: Bearer tt'