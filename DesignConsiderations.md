## Design Considerations

### Database
Application scope is very small to decide concretely either to go with SQL or NoSQL database. So either one should be fine here. 
Although structured data tempted me to go with SQL db, But I went through Mongo DB because
1. The application has opportunity to use plain JSON as response model without much overhead and game data is always linked to its game Id. So document db seems better choice.
2. Any game content can be accessed by its gameId, key-value style db suits here, and as we are accessing by key, it's fast.
3. As of now, there is no need to have searching capabilities on other fields apart from gameId.
4. As its NoSQL database, it helps to scale needs well in the future.

### Cache
Redis cache has been used in the application that helps fast retrieval of game data and reduce the load main database.
cache eviction has been used to clear games that are completed to better manage cache storage and performance.

### Authentication & Authorization
It's always required to have some sort of authentication and authorization for the exposed apis in the project. But in companies, this responsibility
is handled by other services like API gateway or dedicated services for this functionality, which is better way of doing it. 
So I haven't implemented that part and due to time constraint, this service assumes the requests are routed from API gateway.

Same goes with @CrossOrigin annotation on controller, it could not be needed if requests are proxied through API gateway.

### Monitoring & Alerting
Basic metrics are being exposed through prometheus dependency in project, 
but it can be optimized to have some custom metrics and grafana setup to view the metrics, application health.
Based on criticality of the application, alerting can be done through prometheus or pingdom setup or some fancy 3rd party solutions.

### Discovery & Load balancing
Discovery and Load balancing responsibilities can be handled through microservices or by external services like kubernetes. 
I feel it's better to be managed by kubernetes as it can do much better and control it well. So that part has been skipped here.

### Distributed tracing
Distributed tracing can be implemented with Spring Cloud Sleuth, combined with Zipkin in this project. 
But I haven't implemented it yet I do not see it as must to have thing, because the api flow is handled in one service and
its not flowing through multiple services to serve a request and the scope of work happening within the apis,  it's ok tp not to have it, 
but its always good to have feature in this particular scenario.
Another minimal way we can also go with is traceId is generated in Api Gateway and log it in all the services along the way.

### Logging
Basic logging has been used and is logging with all required information to understand whats going on in application. 
But I would like to stream logs to some message broker, then capture them to elasticsearch for better use and maintain of logs.
But that seemed out of scope of this project.

