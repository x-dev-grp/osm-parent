package com.xdev.communicator.feignServices;

import com.xdev.communicator.exceptions.ServiceException;
import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.models.common.dtos.BaseDto;
import com.xdev.communicator.models.common.dtos.apiDTOs.ApiResponse;
import com.xdev.communicator.models.common.dtos.apiDTOs.ApiSingleResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BaseFeignService <INDTO  extends BaseDto,OUTDTO extends BaseDto>{
    public static final Logger log = LoggerFactory.getLogger(BaseFeignService.class);

    protected final BaseFeignController< INDTO, OUTDTO> baseFeignController;

    @Autowired(required = false)
    @Qualifier("feignExecutor")
    public Executor feignExecutor;

    public BaseFeignService(BaseFeignController<INDTO, OUTDTO> baseFeignController) {
        this.baseFeignController = baseFeignController;
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "findByIdFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ApiSingleResponse<OUTDTO>> findById(UUID id) {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiSingleResponse<OUTDTO> response = baseFeignController.findDtoByUuid(id).getBody();
                    validateResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error finding entity by id: {}", id, e);
                    throw new ServiceException("Failed to find entity", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiSingleResponse<OUTDTO> response = baseFeignController.findDtoByUuid(id).getBody();
                    validateResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error finding entity by id: {}", id, e);
                    throw new ServiceException("Failed to find entity", e);
                }
            });
        }
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "fetchAllFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ApiResponse<OUTDTO>> fetchAll() {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiResponse<OUTDTO> response = baseFeignController.fetchAll().getBody();
                    validateListResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error fetching all entities", e);
                    throw new ServiceException("Failed to fetch all entities", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiResponse<OUTDTO> response = baseFeignController.fetchAll().getBody();
                    validateListResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error fetching all entities", e);
                    throw new ServiceException("Failed to fetch all entities", e);
                }
            });
        }
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "fetchAllPageableFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ApiResponse<OUTDTO>> fetchAllPageable(int page, int size, String sort, String direction) {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiResponse<OUTDTO> response = baseFeignController.fetchAllPageable(page, size, sort, direction).getBody();
                    validateListResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error fetching pageable entities with page: {}, size: {}", page, size, e);
                    throw new ServiceException("Failed to fetch pageable entities", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiResponse<OUTDTO> response = baseFeignController.fetchAllPageable(page, size, sort, direction).getBody();
                    validateListResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error fetching pageable entities with page: {}, size: {}", page, size, e);
                    throw new ServiceException("Failed to fetch pageable entities", e);
                }
            });
        }
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "createFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ApiSingleResponse<OUTDTO>> create(INDTO dto) {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiSingleResponse<OUTDTO> response = baseFeignController.create(dto).getBody();
                    validateResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error creating entity: {}", dto, e);
                    throw new ServiceException("Failed to create entity", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiSingleResponse<OUTDTO> response = baseFeignController.create(dto).getBody();
                    validateResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error creating entity: {}", dto, e);
                    throw new ServiceException("Failed to create entity", e);
                }
            });
        }
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "updateFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ApiSingleResponse<OUTDTO>> update(INDTO dto) {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiSingleResponse<OUTDTO> response = baseFeignController.update(dto).getBody();
                    validateResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error updating entity: {}", dto, e);
                    throw new ServiceException("Failed to update entity", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ApiSingleResponse<OUTDTO> response = baseFeignController.update(dto).getBody();
                    validateResponse(response);
                    return response;
                } catch (Exception e) {
                    log.error("Error updating entity: {}", dto, e);
                    throw new ServiceException("Failed to update entity", e);
                }
            });
        }
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "removeFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ResponseEntity<?>> remove(UUID id) {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ResponseEntity<?> response = baseFeignController.remove(id);
                    if (response.getStatusCode().isError()) {
                        throw new ServiceException("Failed to remove entity with id: " + id);
                    }
                    return response;
                } catch (Exception e) {
                    log.error("Error removing entity with id: {}", id, e);
                    throw new ServiceException("Failed to remove entity", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ResponseEntity<?> response = baseFeignController.remove(id);
                    if (response.getStatusCode().isError()) {
                        throw new ServiceException("Failed to remove entity with id: " + id);
                    }
                    return response;
                } catch (Exception e) {
                    log.error("Error removing entity with id: {}", id, e);
                    throw new ServiceException("Failed to remove entity", e);
                }
            });
        }
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "deleteFallback")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ResponseEntity<?>> delete(UUID id) {
        if (feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ResponseEntity<?> response = baseFeignController.delete(id);
                    if (response.getStatusCode().isError()) {
                        throw new ServiceException("Failed to delete entity with id: " + id);
                    }
                    return response;
                } catch (Exception e) {
                    log.error("Error deleting entity with id: {}", id, e);
                    throw new ServiceException("Failed to delete entity", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    ResponseEntity<?> response = baseFeignController.delete(id);
                    if (response.getStatusCode().isError()) {
                        throw new ServiceException("Failed to delete entity with id: " + id);
                    }
                    return response;
                } catch (Exception e) {
                    log.error("Error deleting entity with id: {}", id, e);
                    throw new ServiceException("Failed to delete entity", e);
                }
            });
        }
    }


    // Fallback methods
    public CompletableFuture<ApiResponse<OUTDTO>> fetchAllFallback(Exception ex) {
        log.warn("Fallback triggered for fetchAll, error: {}", ex.getMessage());
        return CompletableFuture.completedFuture(
                new ApiResponse<OUTDTO>(Boolean.FALSE, ex.getMessage(),null)
        );
    }

    public CompletableFuture<ApiResponse<OUTDTO>> fetchAllPageableFallback(int page, int size, String sort, String direction, Exception ex) {
        log.warn("Fallback triggered for fetchAllPageable with page: {}, size: {}, error: {}", page, size, ex.getMessage());
        return CompletableFuture.completedFuture(
                new ApiResponse<OUTDTO>(Boolean.FALSE, ex.getMessage(), null)
        );
    }

    public CompletableFuture<ApiSingleResponse<OUTDTO>> createFallback(INDTO dto, Exception ex) {
        log.warn("Fallback triggered for create with dto: {}, error: {}", dto, ex.getMessage());
        return CompletableFuture.completedFuture(
                new ApiSingleResponse<OUTDTO>(Boolean.FALSE, ex.getMessage(), null)
        );
    }

    public CompletableFuture<ApiSingleResponse<OUTDTO>> updateFallback(INDTO dto, Exception ex) {
        log.warn("Fallback triggered for update with dto: {}, error: {}", dto, ex.getMessage());
        return CompletableFuture.completedFuture(
                new ApiSingleResponse<OUTDTO>(Boolean.FALSE, ex.getMessage(), null)
        );
    }

    public CompletableFuture<ResponseEntity<?>> removeFallback(UUID id, Exception ex) {
        log.warn("Fallback triggered for remove with id: {}, error: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service unavailable: " + ex.getMessage())
        );
    }

    public CompletableFuture<ResponseEntity<?>> deleteFallback(UUID id, Exception ex) {
        log.warn("Fallback triggered for delete with id: {}, error: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service unavailable: " + ex.getMessage())
        );
    }


    public CompletableFuture<ApiSingleResponse<OUTDTO>> findByIdFallback(UUID id, Exception ex) {
        log.warn("Fallback triggered for findById with id: {}, error: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(
                new ApiSingleResponse<OUTDTO>(Boolean.FALSE, ex.getMessage(),null)
        );
    }


    protected void validateResponse(ApiSingleResponse<OUTDTO> response) {
        if (response == null || !response.isSuccess()) {
            String errorMessage = response != null ? response.getMessage() : "Unknown error";
            throw new ServiceException("Service call failed: " + errorMessage);
        }
    }
    protected void validateListResponse(ApiResponse<OUTDTO> response) {
        if (response == null || !response.isSuccess()) {
            String errorMessage = response != null ? response.getMessage() : "Unknown error";
            throw new ServiceException("Service call failed: " + errorMessage);
        }
    }

}
