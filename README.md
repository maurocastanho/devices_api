# devices_api

## Devices API

API that manipulates devices.

A device has the following attributes:

* Id
* Name
* Brand
* State (available, in-use, inactive)
* Creation time

API funcionatilites:

* Create a new device.
* Fully and/or partially update an existing device.
* Fetch a single device.
* Fetch all devices.
* Fetch devices by brand.
* Fetch devices by state.
* Delete a single device.

API documentation is available through swagger (link: http://server:8080/api-docs)

The server can be started by the command, in the project root:

    ./mvnw spring-boot:run

To deploy and start the server using a container, use the commands:

    ./mvnw packate
    docker-compose run -p 8080:8080 app

Requests to the API can be made using the port 8080.