package com.xdev.communicator.feignControllers;


import com.xdev.communicator.models.common.dtos.BaseDto;
import com.xdev.communicator.models.common.dtos.apiDTOs.ApiResponse;
import com.xdev.communicator.models.common.dtos.apiDTOs.ApiSingleResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface BaseFeignController<INDTO  extends BaseDto ,OUTDTO extends BaseDto> {


    @GetMapping(value = "/fetch/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiSingleResponse<OUTDTO>> findDtoByUuid(@PathVariable UUID id);

    @GetMapping(value = "/fetchAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<OUTDTO>>   fetchAll();

    @GetMapping(value = "/fetchAllPageable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<OUTDTO>> fetchAllPageable(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false, defaultValue = "createdDate") String sort, @RequestParam(required = false, defaultValue = "DESC") String direction
    );
    @PostMapping
    public ResponseEntity<ApiSingleResponse<OUTDTO>> create(
            @RequestBody INDTO dto
    );
    @PutMapping
    public ResponseEntity<ApiSingleResponse<OUTDTO>> update(
            @RequestBody INDTO dto
    );
    @DeleteMapping("/remove/{id}")
     ResponseEntity<?> remove(@PathVariable UUID id);
    @DeleteMapping("/delete/{id}")
    ResponseEntity<?> delete(@PathVariable UUID id);

}
