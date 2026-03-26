package za.ac.cput.repository.service.impl;
import za.ac.cput.domain.Service;
import za.ac.cput.repository.service.IServiceRepository;
import java.util.HashSet;
import java.util.Set;
/*ServiceRepository.java
 Service Repository  class
 Author: Witcha Francisco (222894822)
 Date: 25/03/2026
*/
public class ServiceRepository  implements IServiceRepository  {

    private static ServiceRepository repository = null;
    private Set<Service> serviceDB;

    private ServiceRepository() {
        serviceDB = new HashSet<>();
    }

    public static ServiceRepository getRepository() {
        if (repository == null) {
            repository = new ServiceRepository();
        }
        return repository;
    }

    @Override
    public Service create(Service service) {
        serviceDB.add(service);
        return service;
    }

    @Override
    public Service read(String id) {
        return serviceDB.stream()
                .filter(s -> s.getServiceId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Service update(Service service) {
        Service oldService = read(service.getServiceId());
        if (oldService != null) {
            serviceDB.remove(oldService);
            serviceDB.add(service);
            return service;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Service service = read(id);
        if (service != null) {
            serviceDB.remove(service);
            return true;
        }
        return false;
    }

    @Override
    public Set<Service> getAll() {
        return serviceDB;
    }

}
