You will need to add the following to you your `/etc/hosts` file

`127.0.0.1       kafka`

1 - Start up your containers
```bash
docker-compose up -d
```

2 - Exec into the container
```bash
docker exec -it poc_kafka_1 bash
```

3 - Create a request directory
```bash
mkdir requests && cd requests
```

4 - Open VI
```bash
vi pay-check-paid.txt
```

5 - Paste the payload
```javascript
{"id":"9e7d3885-b47b-453b-8689-2eb48f53fdc3","specVersion":"2.1","tenantId":"e6dcdc68-0c76-d076-7777-272136a77acf","timestamp":"2016-06-30T12:36:43.843Z","effectiveDatetime":"2016-06-30T12:36:43.843Z","type":"payment.employee-pay-check.paid","schemaVersion":"2.0","contentType":"application/json","body":{"checkNumber":"12345678","payDate":"2016-07-01T12:00:00Z"}}
```

6 - Save and Exit
```bash
:wq
```

7. Run the following to publish the payload

```bash
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic poc < pay-check-paid.txt
```
