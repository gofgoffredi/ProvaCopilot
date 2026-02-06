package ba.com.zira.billing.penalty.core.impl.franchigia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ba.com.zira.billing.penalty.api.franchigia.PenaltyFranchigiaAggregationService;
import ba.com.zira.billing.penalty.api.model.franchigia.PenaltyFranchigiaAggregation;
import ba.com.zira.billing.penalty.api.model.franchigia.PenaltyFranchigiaAggregationCreateRequest;
import ba.com.zira.billing.penalty.api.model.franchigia.PenaltyFranchigiaAggregationUpdateRequest;
import ba.com.zira.billing.penalty.core.impl.LookupService;
import ba.com.zira.billing.penalty.core.validation.franchigia.PenaltyFranchigiaAggregationRequestValidation;
import ba.com.zira.billing.penalty.dao.RegistryPenaltyDAO;
import ba.com.zira.billing.penalty.dao.franchigia.PenaltyFranchigiaAggregationDAO;
import ba.com.zira.billing.penalty.dao.model.RegistryPenaltyEntity;
import ba.com.zira.billing.penalty.dao.model.franchigia.PenaltyFranchigiaAggregationEntity;
import ba.com.zira.billing.penalty.mapper.franchigia.PenaltyFranchigiaAggregationMapper;
import ba.com.zira.commons.exception.ApiException;
import ba.com.zira.commons.message.request.EntityRequest;
import ba.com.zira.commons.message.request.SearchRequest;
import ba.com.zira.commons.message.response.PagedPayloadResponse;
import ba.com.zira.commons.message.response.PayloadResponse;
import ba.com.zira.commons.model.Filter;
import ba.com.zira.commons.model.FilterExpression;
import ba.com.zira.commons.model.FilterExpression.FilterOperation;
import ba.com.zira.commons.model.PagedData;
import ba.com.zira.commons.model.enums.Status;
import ba.com.zira.commons.model.response.ResponseCode;
import ba.com.zira.commons.validation.RequestValidator;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PenaltyFranchigiaAggregationServiceImpl implements PenaltyFranchigiaAggregationService {

    private RequestValidator requestValidator;

    private PenaltyFranchigiaAggregationRequestValidation penaltyFranchigiaAggregationRequestValidator;

    private PenaltyFranchigiaAggregationDAO penaltyFranchigiaAggregationDAO;

    private PenaltyFranchigiaAggregationMapper penaltyFranchigiaAggregationMapper;

    private LookupService lookupService;

    private RegistryPenaltyDAO registryPenaltyDAO;

    @Override
    public PayloadResponse<PenaltyFranchigiaAggregation> create(final EntityRequest<PenaltyFranchigiaAggregationCreateRequest> request)
            throws ApiException {

        penaltyFranchigiaAggregationRequestValidator.validateCreateRequest(request, "createPenaltyFranchigiaAggregation");

        final LocalDateTime date = LocalDateTime.now();

        final PenaltyFranchigiaAggregationCreateRequest penaltyFranchigiaAggregationCreateRequest = request.getEntity();

        PenaltyFranchigiaAggregationEntity penaltyFranchigiaAggregationEntity = penaltyFranchigiaAggregationMapper
                .dtoCreateToEntity(penaltyFranchigiaAggregationCreateRequest);

        penaltyFranchigiaAggregationEntity.setStatus(Status.ACTIVE.getValue());
        penaltyFranchigiaAggregationEntity.setCreated(date);
        penaltyFranchigiaAggregationEntity.setCreatedBy(request.getUser().getUserId());
        penaltyFranchigiaAggregationDAO.persist(penaltyFranchigiaAggregationEntity);

        final PenaltyFranchigiaAggregation penaltyFranchigiaAggregation = penaltyFranchigiaAggregationMapper
                .entityToDto(penaltyFranchigiaAggregationEntity);

        List<PenaltyFranchigiaAggregation> penaltyAggregations = new ArrayList<>();
        penaltyAggregations.add(penaltyFranchigiaAggregation);
        lookupService.lookupPenaltyNames(penaltyAggregations, PenaltyFranchigiaAggregation::getPenaltyId,
                PenaltyFranchigiaAggregation::setPenaltyName);

        return new PayloadResponse<>(request, ResponseCode.OK, penaltyFranchigiaAggregation);
    }

    @Override
    public PayloadResponse<PenaltyFranchigiaAggregation> update(final EntityRequest<PenaltyFranchigiaAggregationUpdateRequest> request)
            throws ApiException {
        penaltyFranchigiaAggregationRequestValidator.validateUpdateRequest(request, "updatePenaltyFranchigiaAggregation");

        final LocalDateTime date = LocalDateTime.now();

        final PenaltyFranchigiaAggregationUpdateRequest penaltyFranchigiaAggregationUpdateRequest = request.getEntity();

        PenaltyFranchigiaAggregationEntity penaltyFranchigiaAggregationEntity = penaltyFranchigiaAggregationDAO
                .findByPK(penaltyFranchigiaAggregationUpdateRequest.getId());

        penaltyFranchigiaAggregationMapper.updateEntityFromUpdateRequest(penaltyFranchigiaAggregationUpdateRequest,
                penaltyFranchigiaAggregationEntity);

        penaltyFranchigiaAggregationEntity.setModified(date);
        penaltyFranchigiaAggregationEntity.setModifiedBy(request.getUser().getUserId());
        penaltyFranchigiaAggregationDAO.merge(penaltyFranchigiaAggregationEntity);

        final PenaltyFranchigiaAggregation penaltyFranchigiaAggregation = penaltyFranchigiaAggregationMapper
                .entityToDto(penaltyFranchigiaAggregationEntity);

        List<PenaltyFranchigiaAggregation> penaltyAggregations = new ArrayList<>();
        penaltyAggregations.add(penaltyFranchigiaAggregation);
        lookupService.lookupPenaltyNames(penaltyAggregations, PenaltyFranchigiaAggregation::getPenaltyId,
                PenaltyFranchigiaAggregation::setPenaltyName);

        return new PayloadResponse<>(request, ResponseCode.OK, penaltyFranchigiaAggregation);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedPayloadResponse<PenaltyFranchigiaAggregation> find(final SearchRequest<String> request) throws ApiException {
        requestValidator.validate(request);
        Filter primaryFilter = request.getFilter();
        List<FilterExpression> listOfExps = primaryFilter.getFilterExpressions();
        FilterExpression penNameExp = null;
        if (listOfExps != null) {
            penNameExp = listOfExps.stream().filter(x -> x.getAttribute().equals("penaltyName")).findFirst().orElse(null);
        }
        if (penNameExp != null) {
            primaryFilter.removeFilterExpression(penNameExp);
            Filter newFilter = new Filter();
            newFilter.addFilterExpression(penNameExp);
            List<RegistryPenaltyEntity> registryPenaltyEntites = registryPenaltyDAO.findAll(newFilter).getRecords();
            FilterExpression newFilterExpression = new FilterExpression();
            newFilterExpression.setAttribute("penaltyId");
            newFilterExpression.setFilterOperation(FilterOperation.IN);
            newFilterExpression.setExpressionValueObject(
                    registryPenaltyEntites.stream().map(RegistryPenaltyEntity::getId).collect(Collectors.toList()));
            primaryFilter.addFilterExpression(newFilterExpression);
        }
        PagedData<PenaltyFranchigiaAggregationEntity> penaltyFranchigiaAggregationEntities = penaltyFranchigiaAggregationDAO
                .findAll(primaryFilter);

        List<PenaltyFranchigiaAggregation> penaltyFranchigiaAggregations = penaltyFranchigiaAggregationMapper
                .entitiesToDtos(penaltyFranchigiaAggregationEntities.getRecords());

        lookupService.lookupPenaltyNames(penaltyFranchigiaAggregations, PenaltyFranchigiaAggregation::getPenaltyId,
                PenaltyFranchigiaAggregation::setPenaltyName);

        return new PagedPayloadResponse<>(request, ResponseCode.OK, penaltyFranchigiaAggregationEntities.getRecordsPerPage(),
                penaltyFranchigiaAggregationEntities.getPage(), penaltyFranchigiaAggregationEntities.getNumberOfPages(),
                penaltyFranchigiaAggregationEntities.getNumberOfRecords(), penaltyFranchigiaAggregations);
    }

    @Override
    public PayloadResponse<PenaltyFranchigiaAggregation> cancel(EntityRequest<Long> request) throws ApiException {
        penaltyFranchigiaAggregationRequestValidator.validateExistsPenaltyFranchigiaAggregation(request, "validateId");

        final LocalDateTime date = LocalDateTime.now();

        PenaltyFranchigiaAggregationEntity penaltyFranchigiaAggregationEntity = penaltyFranchigiaAggregationDAO
                .findByPK(request.getEntity());

        penaltyFranchigiaAggregationEntity.setModified(date);
        penaltyFranchigiaAggregationEntity.setModifiedBy(request.getUser().getUserId());
        penaltyFranchigiaAggregationEntity.setStatus(Status.INACTIVE.getValue());

        penaltyFranchigiaAggregationDAO.merge(penaltyFranchigiaAggregationEntity);

        final PenaltyFranchigiaAggregation penaltyFranchigiaAggregation = penaltyFranchigiaAggregationMapper
                .entityToDto(penaltyFranchigiaAggregationEntity);

        List<PenaltyFranchigiaAggregation> penaltyAggregations = new ArrayList<>();
        penaltyAggregations.add(penaltyFranchigiaAggregation);
        lookupService.lookupPenaltyNames(penaltyAggregations, PenaltyFranchigiaAggregation::getPenaltyId,
                PenaltyFranchigiaAggregation::setPenaltyName);

        return new PayloadResponse<>(request, ResponseCode.OK, penaltyFranchigiaAggregation);
    }

}
