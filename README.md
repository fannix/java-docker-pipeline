# How to run

1. clone this repo to a folder (pipeline for example)
2. cd into pipeline folder
3. Install maven2 if you haven't
3. run `mvn package` to compile and package the app
4. Run a Kafka service on localhost
5. run `sudo docker-compose -f docker-compose-services.yml up --build`

