package za.ac.cput.factory;

import za.ac.cput.domain.Service;
/*ServiceFactory.java
 Service Factory  class
 Author: Witcha Francisco (222894822)
 Date: 26/03/2026
*/
public class ServiceFactory {

        public static Service createService (String Id, String name,double price, int duration) throws IllegalArgumentException {

            if (Id == null || Id.isEmpty())
                throw new IllegalArgumentException("Service ID is required");

            if (name == null || name.isEmpty())
                throw new IllegalArgumentException("Service name is required");

            if (price <= 0)
                throw new IllegalArgumentException("Price must be greater than 0");

            if (duration <= 0)
                throw new IllegalArgumentException("Duration must be greater than 0");


            return new Service.Builder()

                    .setServiceId(Id)
                    .setServiceName(name)
                    .setPrice(price)
                    .setDuration(duration)
                    .build();

        }
    }

