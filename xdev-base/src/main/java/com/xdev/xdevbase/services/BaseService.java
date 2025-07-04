package com.xdev.xdevbase.services;

import com.xdev.xdevbase.apiDTOs.SearchResponse;
import com.xdev.xdevbase.dtos.BaseDto;
import com.xdev.xdevbase.entities.BaseEntity;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.models.ExportDetails;
import com.xdev.xdevbase.models.SearchData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;

import java.util.*;

public interface BaseService<E extends BaseEntity, INDTO extends BaseDto<E>, OUTDTO extends BaseDto<E>> {
    Class<E> getEntityClass();
    Class<INDTO> getInDTOClass();
    Class<OUTDTO> getOutDTOClass();
    OUTDTO findById(UUID id);

    List<OUTDTO> findAll();

    Page<OUTDTO> findAll(int page,int size,String sort,String direction);

    OUTDTO save(INDTO request);

    List<OUTDTO> save(List<INDTO>  request);

    OUTDTO update(INDTO entity);

    void remove(UUID id);
    OUTDTO delete(UUID entity);

    void removeAll(Collection<INDTO> entities);

    Optional<Revision<Integer, E>> findLastRevisionById(UUID id);
     void resolveEntityRelations(E entity);
    Revisions<Integer, E> findRevisionsById(UUID id);
    SearchResponse<E,OUTDTO> search(SearchData searchData);
     byte[] exportToPdf(ExportDetails exportDetails);
    byte[] exportToCsv(ExportDetails exportDetails);
    byte[] exportToExcel(ExportDetails exportDetails);
    default Set<Action> actionsMapping(E entity){
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        return actions;
     }
}
