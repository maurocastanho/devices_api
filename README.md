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

The server can be started by the command:

    ./mvnw spring-boot:run
