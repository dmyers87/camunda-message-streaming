You will need to add the following to you your `/etc/hosts` file

`127.0.0.1       kafka`

This was helpful

`https://medium.com/@TimvanBaarsen/apache-kafka-cli-commands-cheat-sheet-a6f06eac01b`

Start up your containers
docker-compose up -d

Exec into the container

`docker exec -it poc_kafka_1 bash`

Create a request directory

`mkdir requests && cd requests`
Open VI

`vi pay-check-paid.txt`

Paste the payload

`{"id":"9e7d3885-b47b-453b-8689-2eb48f53fdc3","specVersion":"2.1","tenantId":"e6dcdc68-0c76-d076-7777-272136a77acf","timestamp":"2016-06-30T12:36:43.843Z","effectiveDatetime":"2016-06-30T12:36:43.843Z","aggregateId":"5374547d-2310-4190-8b63-0dcedc2138cb","sequenceNumber":1,"type":"payment.employee-pay-check.paid","schemaVersion":"2.0","userId":"16490fb4-c21c-496c-9b35-696171c49651","contentType":"application/json","validation":"valid","dataCenter":"ATL","body":{"checkNumber":"12345678","payDate":"2016-07-01T12:00:00Z"},"metadata":{"downcastedFromVersion":"3.0"},"correlationId":"41c2b6a2-8693-4196-b745-a98a10131071","user":"Marcus Mason"}`

Save and Exit

Run the following to publish the payload

`$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic poc < pay-check-paid.txt`
~~~~