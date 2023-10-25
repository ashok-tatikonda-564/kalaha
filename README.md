## Kalaha : An Ancient two player game

![](https://img.shields.io/badge/java-17-blue?style=for-the-badge&logo=java)
![](https://img.shields.io/badge/Spring%20Boot-3.1.5-blue?style=for-the-badge&logo=spring)
![](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue?style=for-the-badge&logo=spring)
![](https://img.shields.io/badge/Gradle-8.4-blue?style=for-the-badge&logo=gradle)
![](https://img.shields.io/badge/Spring%20Data%20Redis-3.1.5-blue?style=for-the-badge&logo=redis)
![](https://img.shields.io/badge/Spring%20Data%20Mongo-4.1.5-blue?style=for-the-badge&logo=mongodb)

### Kalaha Game and Rules
To know more about the game and its rules please refer to [KalahaGame.md](KalahaGame.md)

### How To Run
make sure startKalahaGame.sh in root directory has execute permission, install docker utils and have docker service running and then type below command from root directory of this project.

``./startKalahaGame.sh``

It pulls and start all required dockers, build application and then open the game UI in your default browser.
The script has delay of 8 seconds to cover this, but it may take longer for first time and shorter for subsequent runs. 
So, before accessing game UI in the browser make sure you see logs where all the required docker's state is "started"

If for any reason UI is not opened in your browser, you can do it by opening the html file [Game UI](src/main/resources/static/kalahaGameUi.html)

### How To Stop
To stop the game and all related dockers, run below command in terminal at root directory of this project

``docker-compose down``

### API documentation
After starting the application, visit below link to view specifications of all apis available in this project.
[http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)

### Access application Metrics, health
Spring actuator and prometheus are enabled in this project. The information can be accessed through below link.
[http://localhost:8081/actuator](http://localhost:8081/actuator)

### Application design considerations
There are some considerations made to develop this application such as the database, cache, features, etc. 
For more information refer to [DesignConsiderations.md](DesignConsiderations.md)
