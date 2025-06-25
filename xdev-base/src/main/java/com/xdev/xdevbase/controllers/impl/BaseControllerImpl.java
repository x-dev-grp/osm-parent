package com.xdev.xdevbase.controllers.impl;

import com.sun.security.auth.UserPrincipal;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.apiDTOs.SearchResponse;
import com.xdev.xdevbase.controllers.BaseController;
import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.dtos.RevisionDto;
import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.ExportDetails;
import com.xdev.xdevbase.models.OSMModule;
import com.xdev.xdevbase.models.SearchData;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.history.Revision;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseControllerImpl<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> implements BaseController<E, INDTO, OUTDTO> {
    protected final BaseService<E, INDTO, OUTDTO> baseService;
    protected final ModelMapper modelMapper;



    public BaseControllerImpl(BaseService<E, INDTO, OUTDTO> baseService, ModelMapper modelMapper) {
        this.baseService = baseService;
        this.modelMapper = modelMapper;
    }

    @Override
    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    @Override
    public BaseService<E, INDTO, OUTDTO> getBaseService() {
        return baseService;
    }

    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> findDtoByUuid(@PathVariable UUID id) {
        try {
            OUTDTO e = baseService.findById(id);
            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "", List.of(e)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<E, OUTDTO>(false, e.getMessage(), null));
        }

    }

    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> fetchAll() {
        try {
            List<OUTDTO> list = baseService.findAll();
            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<E, OUTDTO>(false, e.getMessage(), null));
        }

    }

    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> fetchAllPageable(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false, defaultValue = "createdDate") String sort, @RequestParam(required = false, defaultValue = "DESC") String direction

    ) {
        try {
            Page<OUTDTO> pageResult = baseService.findAll(page, size, sort, direction);
            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "", pageResult.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<E, OUTDTO>(false, e.getMessage(), null));
        }

    }

    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> create(
    @RequestBody INDTO dto
    ) {
        try {
            OUTDTO savedEntity = baseService.save(dto);
            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "", List.of(savedEntity)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<E, OUTDTO>(false, e.getMessage(), null));
        }

    }

    @Override
    public ResponseEntity<ApiResponse<E, OUTDTO>> update(
            @RequestBody INDTO dto
    ) {
        try {
            OUTDTO savedEntity = baseService.update(dto);
            return ResponseEntity.ok(new ApiResponse<E, OUTDTO>(true, "", List.of(savedEntity)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<E, OUTDTO>(false, e.getMessage(), null));
        }

    }
    @Override
    public ResponseEntity<?> remove(
         @PathVariable  UUID id
    ) {
        try {
        baseService.remove(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @Override
    public RevisionDto<E> findLastRevision(@PathVariable UUID id) {
        Optional<Revision<Integer, E>> revisionOptional = baseService.findLastRevisionById(id);
        RevisionDto<E> revisionDto = new RevisionDto<E>();
        if (revisionOptional.isPresent()) {
            Revision<Integer, E> revision = revisionOptional.orElse(null);
            E entity = revision.getEntity();
            revisionDto.setRevisionMetadata(revision.getMetadata());
//            Class<?> OUTDTOClass = (Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass())
//                    .getActualTypeArguments()[3];
            OUTDTO outDto = modelMapper.map(entity, baseService.getOutDTOClass());
            revisionDto.setData(outDto);
        }
        return revisionDto;
    }

    @Override
    public List<RevisionDto<E>> findAllRevisions(@PathVariable UUID id) {
        List<Revision<Integer, E>> listRevision = baseService.findRevisionsById(id).getContent();// bankRepository.findRevisions(id).getContent();
        return listRevision.stream().map(ls -> new RevisionDto<E>(ls.getMetadata(), modelMapper.map(ls.getEntity(), baseService.getOutDTOClass()))).toList();
    }


      @Override
      public ResponseEntity<SearchResponse<E,OUTDTO>> advancedSearch(@RequestBody SearchData searchData, Authentication authentication) {
          final String resource = getResourceName();
          Set<String> actions=extractResourcePermissions(authentication,resource);
          String role=extractResourceRole(authentication);
          SearchResponse<E,OUTDTO> response = baseService.search(searchData);
          List<OUTDTO> dtos = response.getData().stream().map(
                  element -> {
                      E entity=modelMapper.map(element,baseService.getEntityClass());
                      Set<String> filteredActions=baseService.actionsMapping(entity);
                      if(!(role.equalsIgnoreCase("ADMIN"))){
                          filteredActions=filteredActions.stream().filter(
                                  a->actions.contains(a)
                          ).collect(Collectors.toSet());
                      }
                      element.setActions(filteredActions);
                      return element;
                  }
          ).toList();
          response.setData(dtos);
          return ResponseEntity.ok(response);
    }

    private String extractResourceRole(Authentication authentication) {
        // 1) Try to reflectively call getClaims() on the principal
        Object principal = authentication.getPrincipal();
        Map<String,Object> claims = null;
        try {
            Method m = principal.getClass().getMethod("getClaims");
            Object maybeClaims = m.invoke(principal);
            if (maybeClaims instanceof Map<?,?>) {
                claims = (Map<String,Object>) maybeClaims;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // principal doesn’t have getClaims() or something went wrong → we'll ignore
        }

        // 2) From the claims map pull out “authorities” if present
        List<String> rawAuthorities = Collections.emptyList();
        if (claims != null) {
           return claims.get("role").toString();

        }
    return "ADMIN";
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractResourcePermissions(Authentication authentication, String resource) {
        if (authentication == null || resource == null) {
            return Collections.emptySet();
        }

        // 1) Try to reflectively call getClaims() on the principal
        Object principal = authentication.getPrincipal();
        Map<String,Object> claims = null;
        try {
            Method m = principal.getClass().getMethod("getClaims");
            Object maybeClaims = m.invoke(principal);
            if (maybeClaims instanceof Map<?,?>) {
                claims = (Map<String,Object>) maybeClaims;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // principal doesn’t have getClaims() or something went wrong → we'll ignore
        }

        // 2) From the claims map pull out “authorities” if present
        List<String> rawAuthorities = Collections.emptyList();
        if (claims != null) {
            Object auths = claims.get("authorities");
            if (auths instanceof Collection<?>) {
                rawAuthorities = ((Collection<?>) auths).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        }

        // 3) Fallback to Spring’s built-in GrantedAuthority list if we got nothing
        if (rawAuthorities.isEmpty()) {
            rawAuthorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        }

        // 4) Now filter by “:RESOURCE:” and strip off the resource prefix
        String filter = ":" + resource.toUpperCase() + ":";
        return rawAuthorities.stream()
                .filter(Objects::nonNull)
                .filter(auth -> auth.toUpperCase().contains(filter))
                .map(this::extractPermissionFromAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String extractPermissionFromAuthority(String authority) {
        if (authority == null || authority.isEmpty()) {
            return null;
        }
        int lastColon = authority.lastIndexOf(':');
        if (lastColon == -1) {
            return null;
        }
        try {
            return authority.substring( lastColon+1);
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    protected abstract String getResourceName();
    @Override
    public ResponseEntity<byte[]> exportPdf(@RequestBody ExportDetails exportDetails) {
        byte[] content = baseService.exportToPdf(exportDetails);

        // Check if response is zipped (multiple documents)
        boolean isZipped = isZipContent(content);

        HttpHeaders headers = new HttpHeaders();
        String fileName = (exportDetails.getFileName()!=null)?exportDetails.getFileName():"file" + (isZipped ? "_export.zip" : "_export.pdf");
        // headers.setContentDispositionFormData("attachment", fileName);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        MediaType mediaType = isZipped ?
                MediaType.parseMediaType("application/zip") :
                MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(content);
    }

    /**
     * Export search results as CSV
     * @param exportDetails search criteria with optional export fields
     * @return CSV file as byte array (or ZIP file containing multiple CSVs)
     */

    @Override
    public ResponseEntity<byte[]> exportCsv(@RequestBody ExportDetails exportDetails) {
        byte[] content = baseService.exportToCsv(exportDetails);

        // Check if response is zipped (multiple documents)
        boolean isZipped = isZipContent(content);

        HttpHeaders headers = new HttpHeaders();
        String fileName = (exportDetails.getFileName() != null) ? exportDetails.getFileName() : "file";
        fileName += (isZipped ? "_export.zip" : "_export.csv");
        //  headers.setContentDispositionFormData("attachment", fileName);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        MediaType mediaType;
        if (isZipped) {
            mediaType = MediaType.parseMediaType("application/zip");
        } else {
            // Use text/csv;charset=UTF-8 to help with encoding detection
            mediaType = MediaType.parseMediaType("text/csv;charset=UTF-8");

            // Make sure to set the content type properly for Excel to recognize it
            headers.set(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8");
            // Add Excel-specific header to ensure proper opening in Excel
            // This hints to Excel that the file is CSV with semicolon delimiter
            // headers.add("Content-Type", "application/vnd.ms-excel;charset=UTF-8");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(content);
    }

    /**
     * Alternative endpoint for exporting directly to Excel format
     */

    @Override
    public ResponseEntity<byte[]> exportExcel(@RequestBody ExportDetails exportDetails) {
        byte[] content = baseService.exportToExcel(exportDetails);

        // Check if response is zipped (multiple documents)
        boolean isZipped = isZipContent(content);

        HttpHeaders headers = new HttpHeaders();
        String fileName = (exportDetails.getFileName() != null) ? exportDetails.getFileName() : "file";
        fileName += (isZipped ? "_export.zip" : "_export.xlsx");
        //headers.setContentDispositionFormData("attachment", fileName);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        MediaType mediaType;
        if (isZipped) {
            mediaType = MediaType.parseMediaType("application/zip");
        } else {
            // XLSX MIME type
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(content);
    }

    /**
     * Helper method to check if content is a ZIP file
     */
    private boolean isZipContent(byte[] content) {
        if (content == null || content.length < 4) {
            return false;
        }

        // Check for ZIP file signature
        return content[0] == 0x50 && content[1] == 0x4B && content[2] == 0x03 && content[3] == 0x05;
    }

}
