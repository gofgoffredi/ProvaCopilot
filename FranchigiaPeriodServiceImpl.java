package ba.com.zira.billing.penalty.core.impl.franchigia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import org.hibernate.Session;
import ba.com.zira.billing.penalty.api.FileLoaderService;
import ba.com.zira.billing.penalty.api.PenaltyAggregationService;
import ba.com.zira.billing.penalty.api.PenaltyCandidateService;
import ba.com.zira.billing.penalty.api.external.joctopussetup.JoctopusSetupClientService;
import ba.com.zira.billing.penalty.api.external.model.QueueParamRequest;
import ba.com.zira.billing.penalty.api.external.model.QueueRequest;
import ba.com.zira.billing.penalty.api.external.processing.ProcessingService;
import ba.com.zira.billing.penalty.api.external.uaa.UaaService;
import ba.com.zira.billing.penalty.api.franchigia.FranchigiaPeriodService;
import ba.com.zira.billing.penalty.api.model.AggregationGroup;
import ba.com.zira.billing.penalty.api.model.FranchigiaAggregationGroup;
import ba.com.zira.billing.penalty.api.model.Pair;
import ba.com.zira.billing.penalty.api.model.RetrivePeriodIdRequest;
import ba.com.zira.billing.penalty.api.model.enums.AggregationStatus;
import ba.com.zira.billing.penalty.api.model.enums.FrequencyType;
import ba.com.zira.billing.penalty.api.model.enums.MeasureUnit;
import ba.com.zira.billing.penalty.api.model.enums.RegistryPeriodStatus;
import ba.com.zira.billing.penalty.api.model.franchigia.FranchigiaPeriod;
import ba.com.zira.billing.penalty.api.model.franchigia.FranchigiaPeriodCreateRequest;
import ba.com.zira.billing.penalty.api.model.franchigia.HandlePeriodRequest;
import ba.com.zira.billing.penalty.api.model.franchigia.PenaltyAggregationTmp;
import ba.com.zira.billing.penalty.api.model.franchigia.ProcessAggregationCriteriaRequest;
import ba.com.zira.billing.penalty.api.model.franchigia.RetrievePenaltyAggPenaltyItem;
import ba.com.zira.billing.penalty.core.impl.fileload.FranchigiaAggregator;
import ba.com.zira.billing.penalty.core.impl.fileload.SmsRuleCalculationHelper;
import ba.com.zira.billing.penalty.core.utils.CacheManager;
import ba.com.zira.billing.penalty.core.validation.franchigia.FranchigiaPeriodRequestValidation;
import ba.com.zira.billing.penalty.dao.FileLoadHeadPenaltyDAO;
import ba.com.zira.billing.penalty.dao.PenaltyAggregationDAO;
import ba.com.zira.billing.penalty.dao.PenaltyDAO;
import ba.com.zira.billing.penalty.dao.RegistryPenaltyDAO;
import ba.com.zira.billing.penalty.dao.SmsLoadPenaltyDAO;
import ba.com.zira.billing.penalty.dao.franchigia.FranchigiaPeriodDAO;
import ba.com.zira.billing.penalty.dao.franchigia.PenaltyFranchigiaAggregationDAO;
import ba.com.zira.billing.penalty.dao.model.PenaltyAggregationEntity;
import ba.com.zira.billing.penalty.dao.model.PenaltyEntity;
import ba.com.zira.billing.penalty.dao.model.RegistryPenaltyEntity;
import ba.com.zira.billing.penalty.dao.model.custom.CalculationRuleWithDetail;
import ba.com.zira.billing.penalty.dao.model.franchigia.FranchigiaPeriodEntity;
import ba.com.zira.billing.penalty.dao.model.franchigia.PenaltyFranchigiaAggregationEntity;
import ba.com.zira.billing.penalty.dao.franchigia.PenaltyFranchigiaVariantAggregation;
import ba.com.zira.billing.penalty.dao.rule.CalculationRuleDAO;
import ba.com.zira.billing.penalty.dao.rule.ValorizationRuleDAO;
import ba.com.zira.billing.penalty.mapper.franchigia.FranchigiaPeriodMapper;
import ba.com.zira.commons.exception.ApiException;
import ba.com.zira.commons.message.request.AbstractRequest;
import ba.com.zira.commons.message.request.EmptyRequest;
import ba.com.zira.commons.message.request.EntityRequest;
import ba.com.zira.commons.message.request.ListRequest;
import ba.com.zira.commons.message.request.SearchRequest;
import ba.com.zira.commons.message.response.PagedPayloadResponse;
import ba.com.zira.commons.message.response.PayloadResponse;
import ba.com.zira.commons.model.PagedData;
import ba.com.zira.commons.model.User;
import ba.com.zira.commons.model.response.ResponseCode;
import ba.com.zira.commons.validation.RequestValidator;
import ba.com.zira.i18n.MessageTranslator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FranchigiaPeriodServiceImpl implements FranchigiaPeriodService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FranchigiaPeriodServiceImpl.class);

    private RequestValidator requestValidator;
    private FranchigiaPeriodRequestValidation franchigiaPeriodRequestValidator;
    private FranchigiaPeriodDAO franchigiaPeriodDAO;
    private FranchigiaPeriodMapper franchigiaPeriodMapper;
    private PenaltyFranchigiaAggregationDAO penaltyFranchigiaAggregationDAO;
    private PenaltyAggregationDAO penaltyAggregationDAO;
    private PenaltyDAO penaltyDAO;
    private PenaltyAggregationService penaltyAggregationService;
    private SmsLoadPenaltyDAO smsLoadPenaltyDAO;
    private FileLoadHeadPenaltyDAO fileLoadHeadPenaltyDAO;
    private FileLoaderService fileLoaderService;
    private CalculationRuleDAO calculationRuleDAO;
    private ValorizationRuleDAO valorizationRuleDAO;
    private RegistryPenaltyDAO registryPenaltyDAO;
    private JoctopusSetupClientService joctopusSetupClientService;
    private PenaltyCandidateService penaltyCandidateService;
    private FranchigiaAggregator franchigiaAggregator;
    private final MessageTranslator messageTranslator;
    private ProcessingService processingService;

    @Value("${franchigia.close.process.page.size:10000}")
    private int pageSize;

    @Value("${franchigia.close.process.queue.name:FRANCHIGIA_PERIOD_CLOSE}")
    private String queueName;

    @Value("${franchigia.close.process.aggregation.task.id}")
    private String taskId;
    
    @Autowired
    public FranchigiaPeriodServiceImpl(final RequestValidator requestValidator,
            final FranchigiaPeriodRequestValidation franchigiaPeriodRequestValidator, final FranchigiaPeriodDAO franchigiaPeriodDAO,
            final FranchigiaPeriodMapper franchigiaPeriodMapper, final PenaltyFranchigiaAggregationDAO penaltyFranchigiaAggregationDAO,
            final PenaltyAggregationDAO penaltyAggregationDAO, final PenaltyDAO penaltyDAO,
            final PenaltyAggregationService penaltyAggregationService, final SmsLoadPenaltyDAO smsLoadPenaltyDAO,
            final FileLoadHeadPenaltyDAO fileLoadHeadPenaltyDAO, @Lazy final FileLoaderService fileLoaderService,
            final CalculationRuleDAO calculationRuleDAO, final ValorizationRuleDAO valorizationRuleDAO,
            final RegistryPenaltyDAO registryPenaltyDAO, final JoctopusSetupClientService joctopusSetupClientService,
            final PenaltyCandidateService penaltyCandidateService, final FranchigiaAggregator franchigiaAggregator,
            final ProcessingService processingService, final MessageTranslator messageTranslator, final UaaService uaaService,
            final ObjectMapper objectMapper) {
        super();
        this.requestValidator = requestValidator;
        this.franchigiaPeriodRequestValidator = franchigiaPeriodRequestValidator;
        this.franchigiaPeriodDAO = franchigiaPeriodDAO;
        this.franchigiaPeriodMapper = franchigiaPeriodMapper;
        this.penaltyFranchigiaAggregationDAO = penaltyFranchigiaAggregationDAO;
        this.penaltyAggregationDAO = penaltyAggregationDAO;
        this.penaltyDAO = penaltyDAO;
        this.penaltyAggregationService = penaltyAggregationService;
        this.smsLoadPenaltyDAO = smsLoadPenaltyDAO;
        this.fileLoadHeadPenaltyDAO = fileLoadHeadPenaltyDAO;
        this.fileLoaderService = fileLoaderService;
        this.calculationRuleDAO = calculationRuleDAO;
        this.valorizationRuleDAO = valorizationRuleDAO;
        this.registryPenaltyDAO = registryPenaltyDAO;
        this.joctopusSetupClientService = joctopusSetupClientService;
        this.penaltyCandidateService = penaltyCandidateService;
        this.franchigiaAggregator = franchigiaAggregator;
        this.processingService = processingService;
        this.messageTranslator = messageTranslator;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedPayloadResponse<FranchigiaPeriod> find(final SearchRequest<String> request) throws ApiException {
        requestValidator.validate(request);

        PagedData<FranchigiaPeriodEntity> franchigiaPeriodEntities = franchigiaPeriodDAO.findAll(request.getFilter());

        List<FranchigiaPeriod> franchigiaPeriods = franchigiaPeriodMapper.entitiesToDtos(franchigiaPeriodEntities.getRecords());

        return new PagedPayloadResponse<>(request, ResponseCode.OK, franchigiaPeriods.size(), 1, 1, franchigiaPeriods.size(),
                franchigiaPeriods);
    }

    @Override
    public PayloadResponse<FranchigiaPeriod> createCustom(final EntityRequest<FranchigiaPeriodCreateRequest> request) throws ApiException {
        String franchigiaFrequency = request.getEntity().getFranchigiaFrequency();
        FranchigiaPeriodCreateRequest franchigiaPeriodCreateRequest = request.getEntity();
        FranchigiaPeriod franchigiaPeriod = new FranchigiaPeriod();

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (FrequencyType.DAILY.getValue().equals(franchigiaFrequency)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            startDate = franchigiaPeriodCreateRequest.getStartDate();
            endDate = franchigiaPeriodCreateRequest.getStartDate().withHour(23).withMinute(59).withSecond(59);

        } else if (FrequencyType.MONTHLY.getValue().equals(franchigiaFrequency)) {
            startDate = franchigiaPeriodCreateRequest.getStartDate().with(TemporalAdjusters.firstDayOfMonth());
            endDate = franchigiaPeriodCreateRequest.getStartDate().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59)
                    .withSecond(59);

        } else if (FrequencyType.QUARTERLY.getValue().equals(franchigiaFrequency)) {
            if (franchigiaPeriodCreateRequest.getStartDate().getMonthValue() <= 3) {// first
                startDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.JANUARY, 1, 0, 0);

            } else if (franchigiaPeriodCreateRequest.getStartDate().getMonthValue() <= 6) {// second
                startDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.APRIL, 1, 0, 0);

            } else if (franchigiaPeriodCreateRequest.getStartDate().getMonthValue() <= 9) {// third
                startDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.JUNE, 1, 0, 0);

            } else if (franchigiaPeriodCreateRequest.getStartDate().getMonthValue() <= 12) {// fourth
                startDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.OCTOBER, 1, 0, 0);
            }

            endDate = startDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        } else if (FrequencyType.HALFYEARLY.getValue().equals(franchigiaFrequency)) {
            if (franchigiaPeriodCreateRequest.getStartDate().getMonth().ordinal() > 5) {// second
                                                                                        // half
                startDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.JULY, 1, 0, 0);
                endDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.DECEMBER, 31, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            } else {// first half
                startDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.JANUARY, 1, 0, 0);
                endDate = LocalDateTime.of(franchigiaPeriodCreateRequest.getStartDate().getYear(), Month.JUNE, 30, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            }

        } else if (FrequencyType.YEARLY.getValue().equals(franchigiaFrequency)) {
            startDate = franchigiaPeriodCreateRequest.getStartDate().with(TemporalAdjusters.firstDayOfMonth());
            endDate = franchigiaPeriodCreateRequest.getStartDate().with(TemporalAdjusters.lastDayOfMonth()).plusMonths(11).withHour(23)
                    .withMinute(59).withSecond(59);
        }

        String startDateName = startDate.format(formatter);
        String endDateName = endDate.format(formatter);

        String periodName = startDateName + " - " + endDateName;

        franchigiaPeriod.setName(periodName);
        franchigiaPeriod.setStartDate(startDate);
        franchigiaPeriod.setEndDate(endDate);
        franchigiaPeriod.setFranchigiaFrequency(franchigiaFrequency);

        franchigiaPeriodRequestValidator.validateCreateRequest(request, startDate, endDate, "createFranchigiaPeriod");

        return create(request, franchigiaPeriod);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayloadResponse<Long> createRecurring(final EntityRequest<FranchigiaPeriodCreateRequest> request) throws ApiException {
        String billingFrequency = request.getEntity().getFranchigiaFrequency();
        Long periodCount = 0L;

        if (FrequencyType.DAILY.getValue().equals(billingFrequency)) {
            periodCount = createPeriod(request, 1, true);

        } else if (FrequencyType.MONTHLY.getValue().equals(billingFrequency)) {
            periodCount = createPeriod(request, 1, false);

        } else if (FrequencyType.QUARTERLY.getValue().equals(billingFrequency)) {
            periodCount = createPeriod(request, 3, false);

        } else if (FrequencyType.HALFYEARLY.getValue().equals(billingFrequency)) {
            periodCount = createPeriod(request, 6, false);

        } else if (FrequencyType.YEARLY.getValue().equals(billingFrequency)) {
            // periodCount = createPeriod(request, 12, false);
            periodCount = createYearlyPeriod(request);
        }

        return new PayloadResponse<>(request, ResponseCode.OK, periodCount);
    }

    public Long createPeriod(final EntityRequest<FranchigiaPeriodCreateRequest> request, final int countStep, final Boolean isDaily) {
        LocalDateTime endDateFromRequest = request.getEntity().getEndDate().with(TemporalAdjusters.lastDayOfMonth());
        if (FrequencyType.QUARTERLY.getValue().equals(request.getEntity().getFranchigiaFrequency())) {
            if (request.getEntity().getEndDate().getMonthValue() <= 3) {
                endDateFromRequest = LocalDateTime.of(request.getEntity().getEndDate().getYear(), Month.MARCH, 31, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            } else if (request.getEntity().getEndDate().getMonthValue() <= 6) {
                endDateFromRequest = LocalDateTime.of(request.getEntity().getEndDate().getYear(), Month.JUNE, 30, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            } else if (request.getEntity().getEndDate().getMonthValue() <= 9) {
                endDateFromRequest = LocalDateTime.of(request.getEntity().getEndDate().getYear(), Month.SEPTEMBER, 30, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            } else if (request.getEntity().getEndDate().getMonthValue() <= 12) {
                endDateFromRequest = LocalDateTime.of(request.getEntity().getEndDate().getYear(), Month.DECEMBER, 31, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            }

        } else if (FrequencyType.HALFYEARLY.getValue().equals(request.getEntity().getFranchigiaFrequency())) {
            if (request.getEntity().getEndDate().getMonthValue() > 5) {
                endDateFromRequest = LocalDateTime.of(request.getEntity().getEndDate().getYear(), Month.DECEMBER, 31, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            } else {
                endDateFromRequest = LocalDateTime.of(request.getEntity().getEndDate().getYear(), Month.JUNE, 30, 0, 0).withHour(23)
                        .withMinute(59).withSecond(59);
            }

        } else if (FrequencyType.DAILY.getValue().equals(request.getEntity().getFranchigiaFrequency())) {
            endDateFromRequest = request.getEntity().getEndDate().withHour(23).withMinute(59).withSecond(59);
        }

        int i = 0;
        boolean condition = true;
        long periodCount = 0;
        do {
            condition = compareAndCreate(request, countStep - 1, i, endDateFromRequest, isDaily);
            if (condition) {
                periodCount++;
            }
            i = i + countStep;
        } while (condition);
        return periodCount;
    }

    public Long createYearlyPeriod(final EntityRequest<FranchigiaPeriodCreateRequest> request) {
        int startYear = request.getEntity().getStartDate().getYear();
        int endYear = request.getEntity().getEndDate().getYear();
        long periodCount = 0;

        for (int i = startYear; i <= endYear; i++) {
            LocalDateTime startDate = LocalDateTime.of(i, Month.JANUARY, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(i, Month.DECEMBER, 31, 0, 0).withHour(23).withMinute(59).withSecond(59);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String periodName = startDate.format(formatter) + " - " + endDate.format(formatter);

            recurringCreate(request, startDate, endDate, periodName);
            periodCount++;
        }
        return periodCount;
    }

    public boolean compareAndCreate(final EntityRequest<FranchigiaPeriodCreateRequest> request, final int addMonths, final int i,
            final LocalDateTime endDateFromRequest, final Boolean isDaily) {
        LocalDateTime startDate = request.getEntity().getStartDate().plusMonths(i).with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endDate = startDate.plusMonths(addMonths).with(TemporalAdjusters.lastDayOfMonth());

        if (FrequencyType.QUARTERLY.getValue().equals(request.getEntity().getFranchigiaFrequency())) {
            LocalDateTime begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.JANUARY, 1, 0, 0);

            if (request.getEntity().getStartDate().getMonthValue() <= 3) {
                begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.JANUARY, 1, 0, 0);
            } else if (request.getEntity().getStartDate().getMonthValue() <= 6) {
                begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.APRIL, 1, 0, 0);
            } else if (request.getEntity().getStartDate().getMonthValue() <= 9) {
                begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.JULY, 1, 0, 0);
            } else if (request.getEntity().getStartDate().getMonthValue() <= 12) {
                begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.OCTOBER, 1, 0, 0);
            }

            startDate = begginCountDate.plusMonths(i).with(TemporalAdjusters.firstDayOfMonth());
            endDate = startDate.plusMonths(addMonths).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        } else if (FrequencyType.HALFYEARLY.getValue().equals(request.getEntity().getFranchigiaFrequency())) {
            LocalDateTime begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.JANUARY, 1, 0, 0);
            if (request.getEntity().getStartDate().getMonthValue() > 5) {
                begginCountDate = LocalDateTime.of(request.getEntity().getStartDate().getYear(), Month.JULY, 1, 0, 0);
            }
            startDate = begginCountDate.plusMonths(i).with(TemporalAdjusters.firstDayOfMonth());
            endDate = startDate.plusMonths(addMonths).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String periodName = startDate.format(formatter) + " - " + endDate.format(formatter);

        if (isDaily) {
            startDate = request.getEntity().getStartDate().plusDays(i);
            endDate = startDate.withHour(23).withMinute(59).withSecond(59);

            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            periodName = startDate.format(formatter) + " - " + endDate.format(formatter);
        }

        if (endDate.isBefore(endDateFromRequest.plusDays(1))) {
            recurringCreate(request, startDate, endDate, periodName);
        } else {
            return false;
        }
        return true;
    }

    private void recurringCreate(final EntityRequest<FranchigiaPeriodCreateRequest> request, final LocalDateTime startDate,
            final LocalDateTime endDate, final String periodName) {

        final LocalDateTime date = LocalDateTime.now();

        String franchigiaFrequency = request.getEntity().getFranchigiaFrequency();

        FranchigiaPeriod franchigiaPeriod = new FranchigiaPeriod();

        franchigiaPeriod.setName(periodName);
        franchigiaPeriod.setStartDate(startDate);
        franchigiaPeriod.setEndDate(endDate.withHour(23).withMinute(59).withSecond(59));
        franchigiaPeriod.setFranchigiaFrequency(franchigiaFrequency);

        franchigiaPeriodRequestValidator.validateCreateRequest(request, startDate, endDate, "createFranchigiaFrequency");
        FranchigiaPeriodEntity franchigiaPeriodEntity = franchigiaPeriodMapper.dtoToEntity(franchigiaPeriod);
        franchigiaPeriodEntity.setCreatedBy(request.getUser().getUserId());

        franchigiaPeriodEntity.setStatus(RegistryPeriodStatus.OPEN.value());
        franchigiaPeriodEntity.setCreated(date);

        franchigiaPeriodDAO.persist(franchigiaPeriodEntity);

    }

    private PayloadResponse<FranchigiaPeriod> create(final EntityRequest<FranchigiaPeriodCreateRequest> request,
            final FranchigiaPeriod franchigiaPeriod) {
        final LocalDateTime date = LocalDateTime.now();
        FranchigiaPeriodEntity franchigiaPeriodEntity = franchigiaPeriodMapper.dtoToEntity(franchigiaPeriod);
        franchigiaPeriodEntity.setCreatedBy(request.getUser().getUserId());

        franchigiaPeriodEntity.setStatus(RegistryPeriodStatus.OPEN.value());
        franchigiaPeriodEntity.setCreated(date);

        franchigiaPeriodDAO.persist(franchigiaPeriodEntity);

        franchigiaPeriodMapper.updateDto(franchigiaPeriodEntity, franchigiaPeriod);

        return new PayloadResponse<>(request, ResponseCode.OK, franchigiaPeriod);
    }

    @Override
    public PayloadResponse<Long> getFranchigiaPeriodId(final EntityRequest<RetrivePeriodIdRequest> request) throws ApiException

    {
        Long penaltyPeriodId = franchigiaPeriodDAO.getFranchigiaPeriodId(request.getEntity().getInputDate(),
                request.getEntity().getFrequency());

        return new PayloadResponse<>(request, ResponseCode.OK, penaltyPeriodId);
    }

    @Override
    public PayloadResponse<FranchigiaPeriod> getFranchigiaPeriod(final EntityRequest<RetrivePeriodIdRequest> request) throws ApiException

    {
        FranchigiaPeriodEntity franchigiaPeriod = franchigiaPeriodDAO.getFranchigiaPeriod(request.getEntity().getInputDate(),
                request.getEntity().getFrequency());

        return new PayloadResponse<>(request, ResponseCode.OK, franchigiaPeriodMapper.entityToDto(franchigiaPeriod));
    }

    @Override
    public void close(final EntityRequest<Long> request) throws ApiException {

        FranchigiaPeriodEntity franchigiaPeriod = franchigiaPeriodDAO.getFranchigiaPeriod(request.getEntity());
        prepareRuleCache();
        if (FrequencyType.YEARLY.getValue().equalsIgnoreCase(franchigiaPeriod.getFranchigiaFrequency())) {
            handleAnnualErrato(franchigiaPeriod.getEndDate(), request);
        }
        List<Integer> partitionKeys = new ArrayList<>();

        List<PenaltyFranchigiaAggregationEntity> aggCriteriaList = penaltyFranchigiaAggregationDAO.getAllActive();
        for (PenaltyFranchigiaAggregationEntity agg : aggCriteriaList) {
            log.debug("Creating aggregations for criteria {}", agg.getId());
            List<PenaltyAggregationEntity> aggregations = aggregatePenalty(agg, request.getEntity(), request.getUserId());
            log.debug("Created {} aggregations for criteria {}", aggregations.size(), agg.getId());
            log.debug("Vertical processing");
            processVerticallyV2(aggregations, request.getUserId(), false);
            log.debug("Horizontal processing");
            partitionKeys = getPartitionKeys(aggregations);
            processHorizontallyV2(prepareGroupIds(aggregations), partitionKeys, request.getUserId());
            log.debug("Updating aggregations");
            penaltyAggregationDAO.updateStatusofAggregations(
                    aggregations.stream().map(PenaltyAggregationEntity::getId).collect(Collectors.toList()), AggregationStatus.PROCESSED,
                    request.getUserId());
            log.debug("Updated aggregations for criteria {}", agg.getId());
        }

        if (!CollectionUtils.isEmpty(partitionKeys)) {

            try {
                penaltyCandidateService.insertAll(new EntityRequest<>(new HashSet<>(partitionKeys)));
            } catch (ApiException e) {
                LOGGER.error("Error during candidate insert: {}", e.getMessage(), e);
            }

        }

        franchigiaPeriodDAO.closePeriod(request.getUserId(), request.getEntity());
    }

    private List<Integer> getPartitionKeys(final List<PenaltyAggregationEntity> aggregations) {
        List<Integer> partitionKeys = new ArrayList<>();
        for (PenaltyAggregationEntity aggregation : aggregations) {
            partitionKeys.addAll(getPartitionKeysForAggregation(aggregation));
        }
        partitionKeys = partitionKeys.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        return partitionKeys;
    }

    private void handleAnnualErrato(final LocalDateTime endDate, final AbstractRequest request) throws ApiException {
        Long fileLoadHeadId = fileLoadHeadPenaltyDAO.createFileLoadQueued("AnnualErrato", "RITARDO_ESPLETAMENTO_XDSL", "SMS", "Penalty",
                "AnnualErrato", "000", LocalDate.now(), null, request, "0");

        smsLoadPenaltyDAO.generateCopiedSMSForAnnualErratoxDSL(endDate, fileLoadHeadId);

        Long fileLoadHeadIdReg = fileLoadHeadPenaltyDAO.createFileLoadQueued("AnnualErrato", "RITARDO_ESPLETAMENTO_REG", "SMS", "Penalty",
                "AnnualErrato", "000", LocalDate.now(), null, request, "0");

        smsLoadPenaltyDAO.generateCopiedSMSForAnnualErratorRegolatorio(endDate, fileLoadHeadIdReg);

        fileLoaderService.sendToProcessingV2(new EmptyRequest(request));
    }

    private void handleAnnualErratoRecalculate(final LocalDateTime endDate, final PenaltyAggregationEntity agg,
            final AbstractRequest request) throws ApiException {
        Long fileLoadHeadId = fileLoadHeadPenaltyDAO.createFileLoadQueued("AnnualErrato", "RITARDO_ESPLETAMENTO_XDSL", "SMS", "Penalty",
                "AnnualErrato", "000", LocalDate.now(), null, request, "0");

        smsLoadPenaltyDAO.generateCopiedSMSForAnnualErratoxDSLRecalculate(endDate, agg.getAggregationCriteria(), agg.getValues(),
                fileLoadHeadId);

        Long fileLoadHeadIdReg = fileLoadHeadPenaltyDAO.createFileLoadQueued("AnnualErrato", "RITARDO_ESPLETAMENTO_REG", "SMS", "Penalty",
                "AnnualErrato", "000", LocalDate.now(), null, request, "0");

        smsLoadPenaltyDAO.generateCopiedSMSForAnnualErratorRegolatorioRecalculate(endDate, agg.getAggregationCriteria(), agg.getValues(),
                fileLoadHeadIdReg);

        fileLoaderService.sendToProcessingV2(new EmptyRequest(request));
    }

    private List<Integer> recalculateProcessing(final PenaltyAggregationEntity agg, final String userId) throws ApiException {
        List<PenaltyAggregationEntity> aggregations = new ArrayList<>();
        FranchigiaPeriodEntity franchigiaPeriod = franchigiaPeriodDAO.getFranchigiaPeriod(agg.getFranchigiaPeriodId());
        prepareRuleCache();
        if (FrequencyType.YEARLY.getValue().equalsIgnoreCase(franchigiaPeriod.getFranchigiaFrequency())) {
            EntityRequest<Long> request = new EntityRequest<>(agg.getFranchigiaPeriodId());
            User user = new User();
            user.setUserId(userId);
            request.setUser(user);
            handleAnnualErratoRecalculate(franchigiaPeriod.getEndDate(), agg, request);
        }
        aggregations.add(agg);
        penaltyDAO.clearPrevousCalculations(agg.getId());

        processVerticallyV2(aggregations, userId, true);
        List<Integer> partitionKeys = getPartitionKeys(aggregations);
        processHorizontallyV2(prepareGroupIds(aggregations), partitionKeys, userId);
        penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), null, userId);

        return partitionKeys;
    }

    @Override
    public Long getPenaltyAggregationExistingOrNewOne(final EntityRequest<RetrievePenaltyAggPenaltyItem> request) throws ApiException {
        Long aggregationId = null;
        List<PenaltyAggregationEntity> aggregations = penaltyAggregationDAO
                .getPeriodAggregations(request.getEntity().getFranchigiaPeriodId());
        for (PenaltyAggregationEntity agg : aggregations) {
            List<AggregationGroup> aggregatedPenalties = penaltyDAO.executeSelectAggregationQuery(generateAggregateSelectQueryForSingleItem(
                    agg, request.getEntity().getFranchigiaPeriodId(), request.getEntity().getPenaltyId()));
            if (aggregatedPenalties != null && !aggregatedPenalties.isEmpty()) {
                aggregationId = agg.getId();
            }
        }

        if (aggregationId != null) {
            penaltyAggregationDAO.updateIndicatorReceived(aggregationId, "U");
        }

        return aggregationId;
    }

    @Override
    public void closedPeriodRecalculate(final EmptyRequest request) throws ApiException {
        // penaltyDAO.setLocalBuffer();
        List<PenaltyAggregationEntity> aggregations = penaltyAggregationDAO.getAllCandidatesForRecalculate("R");
        List<Integer> partitionKeys = new ArrayList<Integer>();
        for (PenaltyAggregationEntity agg : aggregations) {
            if (penaltyDAO.inApproval(agg.getId())) {
                penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), "U", request.getUserId());
            } else {
                partitionKeys.addAll(recalculateProcessing(agg, request.getUserId()));
            }
        }

        List<PenaltyAggregationEntity> aggregationsU = penaltyAggregationDAO.getAllCandidatesForRecalculate("U");

        for (PenaltyAggregationEntity agg : aggregationsU) {
            if (penaltyDAO.approvedOrPreviouslyApproved(agg.getId())) {
                penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), "P", request.getUserId());
                EntityRequest<Long> req = new EntityRequest<Long>(agg.getId());
                req.setUser(request.getUser());
                penaltyAggregationService.sendToApproval(req);
            } else if (penaltyDAO.inApproval(agg.getId())) {
                penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), "U", request.getUserId());
            } else {
                partitionKeys.addAll(recalculateProcessing(agg, request.getUserId()));
            }
        }

        if (!CollectionUtils.isEmpty(partitionKeys)) {

            try {
                penaltyCandidateService.insertAll(new EntityRequest<>(new HashSet<>(partitionKeys)));
            } catch (ApiException e) {
                LOGGER.error("Error during candidate insert: {}", e.getMessage(), e);
            }

        }

    }

    @Override
    public void prepareJoQueueForRecalculate(final EntityRequest<String> request) throws ApiException {
        franchigiaPeriodRequestValidator.validatePrepareJoQueueForRecalculate(request);
        List<Long> aggregations = new ArrayList<>();
        if ("R".equals(request.getEntity())) {
            aggregations = penaltyAggregationDAO.getAllCandidatesForRecalculateIds("R");
        } else if ("U".equals(request.getEntity())) {
            aggregations = penaltyAggregationDAO.getAllCandidatesForRecalculateIds("U");
        }
        if (CollectionUtils.isEmpty(aggregations)) {
            log.info("prepareJoQueueForRecalculate {} is empty", request.getEntity());
            aggregations.add(0L);
        }
        log.info("prepareJoQueueForRecalculate {} all aggIds: {} ", request.getEntity(), aggregations);
        processingService.sendRecalculate(new ListRequest<>(aggregations, request));
    }

    @Override
    public void closedPeriodRecalculateAggregation(final EntityRequest<Long> request) throws ApiException {
        requestValidator.validate(request);
        if (request.getEntity() != null && request.getEntity().equals(0L)) {
            log.info("Skipping 0 aggregationId");
            return;
        }
        PenaltyAggregationEntity agg = penaltyAggregationDAO.findByPK(request.getEntity());
        List<Integer> partitionKeys = new ArrayList<Integer>();
        if ("R".equals(agg.getUpdateReceivedFlag())) {
            if (penaltyDAO.inApproval(agg.getId())) {
                penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), "U", request.getUserId());
            } else {
                log.info("START recalculation CHECK R for aggId: {} ", agg.getId());
                partitionKeys.addAll(recalculateProcessing(agg, request.getUserId()));
                log.info("END recalculation CHECK R for aggId: {} ", agg.getId());
            }
        } else if ("U".equals(agg.getUpdateReceivedFlag())) {
            if (penaltyDAO.approvedOrPreviouslyApproved(agg.getId())) {
                penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), "P", request.getUserId());
                EntityRequest<Long> req = new EntityRequest<Long>(agg.getId());
                req.setUser(request.getUser());
                // penaltyAggregationService.sendToApproval(req);

                penaltyAggregationService.sendToApprovalWithUser(req);
            } else if (penaltyDAO.inApproval(agg.getId())) {
                penaltyAggregationDAO.updateIndicatorReceivedOnRecalculate(agg.getId(), "U", request.getUserId());
            } else {
                log.info("START recalculation CHECK U for aggId: {} ", agg.getId());
                partitionKeys.addAll(recalculateProcessing(agg, request.getUserId()));
                log.info("END recalculation CHECK U for aggId: {} ", agg.getId());
            }
        }
        if (!CollectionUtils.isEmpty(partitionKeys)) {
            try {
                penaltyCandidateService.insertAll(new EntityRequest<>(new HashSet<>(partitionKeys)));
            } catch (ApiException e) {
                LOGGER.error("Error during aggId:{}, candidate insert: {}", request.getEntity(), e.getMessage(), e);
            }
        }
    }

    private void processHorizontally(final List<Long> groupIds, final String userId) {
        List<PenaltyEntity> penalties = penaltyDAO.findByAggregationIds(groupIds);

        for (PenaltyEntity penalty : penalties) {
            updatePenaltyAccordingToVerticalProcessResults(userId, penalty);
            updatePenaltyTotalDelayAfterProcessing(userId, penalty);
        }

        List<PenaltyEntity> massivePenalties = penaltyDAO.findByAggregationIdsForMassive(groupIds);

        for (PenaltyEntity penalty : massivePenalties) {
            if (!penalty.getInSla3().equalsIgnoreCase("NO")) {
                updatePenaltyAccordingToVerticalProcessResultsForMassive(userId, penalty);
                updatePenaltyTotalDelayAfterProcessing(userId, penalty);
            }
        }

        mergeAll(penalties);
        mergeAll(massivePenalties);
    }

    private void processHorizontallyV2(final List<Long> groupIds, final List<Integer> partitonKeys, final String userId) {
        log.debug("Starting ordinary processing");
        processHorizontallyOridnaryFranchigia(groupIds, partitonKeys, userId);
        log.debug("Starting massive processing");
        processHorizontallyMasiveFranchigia(groupIds, partitonKeys, userId);
    }

    private void processHorizontallyOridnaryFranchigia(final List<Long> groupIds, final List<Integer> partitonKeys, final String userId) {
        int page = 1;
        penaltyDAO.createTempTableForFranchigia();
        while (true) {
            log.debug("Retrieving page {}", page);
            List<PenaltyEntity> penalties = penaltyDAO.findByAggregationIds(groupIds, partitonKeys, page, pageSize);
            log.debug("Retrieved page {} with {} items  for ordinary processing", page, penalties.size());
            if (penalties.isEmpty()) {
                break;
            }
            for (PenaltyEntity penalty : penalties) {
                updatePenaltyAccordingToVerticalProcessResults(userId, penalty);
                updatePenaltyTotalDelayAfterProcessing(userId, penalty);
            }
            penaltyDAO.clear();
            log.debug("Inserting into helper table page : {}", page);
            penaltyDAO.updatePenaltyRecordsForHorizontalProcessing(penalties, userId);
            log.debug("Inserted into helper table page : {}", page);
            page++;
        }
        log.debug("Final update from TEMP table");
        for (Integer integer : penaltyDAO.getPkeysFranchigia()) {
            penaltyDAO.updateFromHelperTableAndDropTempTableForFranchigia(userId, integer);
        }
        penaltyDAO.dropFranchigiaUpdateHelper();
        log.debug("Final update from TEMP table - FINISH");
    }

    private void processHorizontallyMasiveFranchigia(final List<Long> groupIds, final List<Integer> partitonKeys, final String userId) {

        int page = 1;
        penaltyDAO.createTempTableForMassiveFranchigia();
        while (true) {
            log.debug("Retrieving page {}", page);
            List<PenaltyEntity> penalties = penaltyDAO.findByAggregationIdsForMassive(groupIds, partitonKeys, page, pageSize);
            log.debug("Retrieved page {} with {} items  for massive processing", page, penalties.size());
            if (penalties.isEmpty()) {
                break;
            }
            for (PenaltyEntity penalty : penalties) {
                updatePenaltyAccordingToVerticalProcessResultsForMassive(userId, penalty);
                updatePenaltyTotalDelayAfterProcessing(userId, penalty);
            }
            penaltyDAO.clear();
            log.debug("Inserting into helper table page : {}", page);
            penaltyDAO.updatePenaltyRecordsForMassiveHorizontalProcessing(penalties, userId);
            log.debug("Inserted into helper table page : {}", page);
            page++;
        }
        log.debug("Final update from TEMP table");
        penaltyDAO.updateFromMassiveHelperTableAndDropTempTableForFranchigia(userId);
        log.debug("Final update from TEMP table - FINISH");
    }

    private void calculatePenaltyAmounts12(final PenaltyEntity penalty) {
        PenaltyEntity newPen = new PenaltyEntity();
        newPen.setAverageChargeAmount(penalty.getAverageChargeAmount() != null ? penalty.getAverageChargeAmount() : BigDecimal.ZERO);
        newPen.setTotalDelay(penalty.getAverageDelayDays1() != null ? penalty.getAverageDelayDays1() : BigDecimal.ZERO);
        penalty.setPenaltyAmount1(calculateAmount(penalty.getCalculationRuleId(), newPen));
        newPen.setTotalDelay(penalty.getAverageDelayDays2() != null ? penalty.getAverageDelayDays2() : BigDecimal.ZERO);
        penalty.setPenaltyAmount2(calculateAmount(penalty.getCalculationRuleId(), newPen));

    }

    private void calculatePenaltyAmounts1(final PenaltyEntity penalty) {
        PenaltyEntity newPen = new PenaltyEntity();
        newPen.setAverageChargeAmount(penalty.getAverageChargeAmount() != null ? penalty.getAverageChargeAmount() : BigDecimal.ZERO);
        newPen.setTotalDelay(penalty.getAverageDelayDays1() != null ? penalty.getAverageDelayDays1() : BigDecimal.ZERO);
        penalty.setPenaltyAmount1(calculateAmount(penalty.getCalculationRuleId(), newPen));

    }

    private void updatePenaltyAccordingToVerticalProcessResultsForMassive(final String userId, final PenaltyEntity penalty) {
        if ("SI".equalsIgnoreCase(penalty.getInSla3()) && penalty.getCalculationRuleId() != null) {
            calculatePenaltyAmounts12(penalty);
        }

        else if ("NO".equalsIgnoreCase(penalty.getInSla3()) && penalty.getSlaThresholdValue().intValue() == 100) {
            penalty.setPenaltyAmount1(BigDecimal.ZERO);
            penalty.setPenaltyAmount2(BigDecimal.ZERO);
        }

        else if (penalty.getInSla3() == null && penalty.getCalculationRuleId() != null && "SI".equalsIgnoreCase(penalty.getInSla2())) {
            calculatePenaltyAmounts12(penalty);
        } else {
            penalty.setPenaltyAmount1(BigDecimal.ZERO);
            // penalty.setPenaltyAmount2(BigDecimal.ZERO);
        }

        if (penalty.getInFranchigia2() != null && "NO".equalsIgnoreCase(penalty.getInFranchigia2())
                && "SI".equalsIgnoreCase(penalty.getInSla3())) {
            penalty.setTotalDelay(penalty.getAverageDelayDays2());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount2());
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "SI".equalsIgnoreCase(penalty.getInFranchigia2()) && "SI".equalsIgnoreCase(penalty.getInFranchigia1())
                && "SI".equalsIgnoreCase(penalty.getInSla3())) {
            penalty.setTotalDelay(penalty.getAverageDelayDays2());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "SI".equalsIgnoreCase(penalty.getInFranchigia2()) && "SI".equalsIgnoreCase(penalty.getInFranchigia1())
                && "NO".equalsIgnoreCase(penalty.getInSla3())) {
            penalty.setTotalDelay(BigDecimal.ZERO);
            penalty.setPenaltyAmount(BigDecimal.ZERO);
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "SI".equalsIgnoreCase(penalty.getInFranchigia2()) && "NO".equalsIgnoreCase(penalty.getInFranchigia1())) {
            penalty.setTotalDelay(penalty.getAverageDelayDays2());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount2());
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia2()) && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia1())
                && !"NO".equalsIgnoreCase(penalty.getFictiveProjectCode())) {
            penalty.setTotalDelay(BigDecimal.ZERO);
            penalty.setPenaltyAmount(BigDecimal.ZERO);
        } else if (penalty.getInFranchigia2() == null && penalty.getInFranchigia1() != null
                && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia1()) && !"NO".equalsIgnoreCase(penalty.getFictiveProjectCode())) {
            penalty.setTotalDelay(BigDecimal.ZERO);
            penalty.setPenaltyAmount(BigDecimal.ZERO);
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia2()) && "NO".equalsIgnoreCase(penalty.getInFranchigia1())) {
            penalty.setTotalDelay(penalty.getAverageDelayDays1());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1());
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "SI".equalsIgnoreCase(penalty.getInFranchigia1()) && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia2())) {
            penalty.setTotalDelay(penalty.getAverageDelayDays1());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null
                && "SI".equalsIgnoreCase(penalty.getInFranchigia1()) && "NO".equalsIgnoreCase(penalty.getInFranchigia2())) {
            penalty.setTotalDelay(penalty.getAverageDelayDays2());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount2());
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI") && penalty.getInFranchigia2() == null
                && "SI".equalsIgnoreCase(penalty.getInSla2())) {
            penalty.setPenaltyAmount(BigDecimal.ZERO);
        }

        if ("IN SLA".equalsIgnoreCase(penalty.getInFranchigia2()) || "IN SLA".equalsIgnoreCase(penalty.getInFranchigia1())) {
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        }

        if ("SI".equalsIgnoreCase(penalty.getInSla1()) && "SI".equalsIgnoreCase(penalty.getInSla2()) && penalty.getInSla3() == null) {
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());

        }

        if ("NO".equalsIgnoreCase(penalty.getInFranchigia1()) && penalty.getInFranchigia2() == null
                && "SI".equalsIgnoreCase(penalty.getInSla2()) && "NO".equalsIgnoreCase(penalty.getInSla1())
                && penalty.getInSla3() == null) {
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1());
            penalty.setTotalDelay(penalty.getAverageDelayDays1());
        }

        if (penalty.getTotalDelay() == null) {
            penalty.setTotalDelay(BigDecimal.ZERO);
        }
        if (penalty.getPenaltyAmount() == null) {
            penalty.setPenaltyAmount(BigDecimal.ZERO);
        }

        if (MeasureUnit.DELAY_WORKING_DAYS.value().equalsIgnoreCase(penalty.getElementInPenalty()) || MeasureUnit.DELAY_CALENDAR_DAYS.value().equalsIgnoreCase(penalty.getElementInPenalty())
                && !penalty.getResetIndicator().equals("1")) {
            penalty.setDelayDaysTim(penalty.getTotalDelay());
        } else if ("opera".equalsIgnoreCase(penalty.getSystemCode())
                && (MeasureUnit.DELAY_CALENDAR_HOURS.value().equalsIgnoreCase(penalty.getElementInPenalty())
                        || MeasureUnit.DELAY_WORKING_HOURS.value().equalsIgnoreCase(penalty.getElementInPenalty()))) {
            penalty.setDelayHoursTim(penalty.getTotalDelay());
        }
    }

    private void updatePenaltyAccordingToVerticalProcessResults(final String userId, final PenaltyEntity penalty) {
        if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI") && penalty.getPenaltyAmount1() != null
                && penalty.getInFranchigia2() != null && penalty.getInFranchigia2().equals("SI") && penalty.getPenaltyAmount2() != null) {
            if (penalty.getPenaltyAmount3() != null && !penalty.getPenaltyAmount3().equals(BigDecimal.ZERO) && penalty.getInSla3() != null
                    && ("NO").equalsIgnoreCase(penalty.getInSla3())) {
                penalty.setTotalDelay(penalty.getTotalDelay3());
                penalty.setPenaltyAmount(penalty.getPenaltyAmount3() != null ? penalty.getPenaltyAmount3() : BigDecimal.ZERO);
                penalty.setSlaThresholdValue(penalty.getSlaThresholdValue3());
            } else if ("NO".equalsIgnoreCase(penalty.getInSla1()) && "NO".equalsIgnoreCase(penalty.getInSla2())
                    && "SI".equalsIgnoreCase(penalty.getInSla3())) {
                penalty.setTotalDelay(penalty.getTotalDelay1());
                penalty.setPenaltyAmount(BigDecimal.ZERO);
                penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
            } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia2().equals("SI")) {
                penalty.setTotalDelay(penalty.getTotalDelay2());
                penalty.setPenaltyAmount(penalty.getPenaltyAmount2() != null ? penalty.getPenaltyAmount2() : BigDecimal.ZERO);
                penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
            } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI")
                    && penalty.getInFranchigia2().equals("NO")) {
                penalty.setTotalDelay(penalty.getTotalDelay2());
                penalty.setPenaltyAmount(penalty.getPenaltyAmount2() != null ? penalty.getPenaltyAmount2() : BigDecimal.ZERO);
                penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
            } else if (penalty.getInFranchigia2() != null && penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI")
                    && penalty.getInFranchigia2().equals("IN SLA")) {
                penalty.setTotalDelay(penalty.getTotalDelay1());
                penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
                penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
            } else {
                penalty.setTotalDelay(penalty.getTotalDelay1());
                penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
                penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());

            }
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI") && penalty.getInFranchigia2() == null
                && "NO".equalsIgnoreCase(penalty.getInSla2()) && !"3".equalsIgnoreCase(penalty.getPenaltyCode())) {
            penalty.setTotalDelay(penalty.getTotalDelay2());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount2() != null ? penalty.getPenaltyAmount2() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI")
                && "SI".equalsIgnoreCase(penalty.getInSla2())) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI") && penalty.getInFranchigia2() == null) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("IN SLA") && penalty.getInFranchigia2() == null
                && !"NO".equalsIgnoreCase(penalty.getFictiveProjectCode())) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("IN SLA") && penalty.getInFranchigia2() != null
                && penalty.getInFranchigia2().equals("IN SLA") && !"NO".equalsIgnoreCase(penalty.getFictiveProjectCode())) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("NO") && penalty.getInFranchigia2() == null) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI")
                && "NO".equalsIgnoreCase(penalty.getInFranchigia2())) {
            penalty.setTotalDelay(penalty.getTotalDelay2());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount2() != null ? penalty.getPenaltyAmount2() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue2());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI")
                && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia2())) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("NO")
                && "NO".equals(penalty.getInFranchigia2())) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("NO")
                && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia2())) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("NO")) {
            penalty.setTotalDelay(penalty.getTotalDelay1());
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if (penalty.getInFranchigia1() != null && penalty.getInFranchigia1().equals("SI") && penalty.getInFranchigia2() == null) {
            penalty.setPenaltyAmount(BigDecimal.ZERO);
            penalty.setTotalDelay(BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue1());
        } else if ("IN SLA".equalsIgnoreCase(penalty.getInFranchigia1()) && "IN SLA".equalsIgnoreCase(penalty.getInFranchigia2())
                && "SI".equalsIgnoreCase(penalty.getInSla3()) && !"NO".equalsIgnoreCase(penalty.getFictiveProjectCode())) {
            penalty.setTotalDelay(BigDecimal.ZERO);
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(penalty.getSlaThresholdValue3());
        } else {
            penalty.setTotalDelay(BigDecimal.ZERO);
            penalty.setPenaltyAmount(penalty.getPenaltyAmount1() != null ? penalty.getPenaltyAmount1() : BigDecimal.ZERO);
            penalty.setSlaThresholdValue(null);
        }
        penalty.setModified(LocalDateTime.now());
        penalty.setModifiedBy(userId);

        if (penalty.getTotalDelay() == null) {
            penalty.setTotalDelay(BigDecimal.ZERO);
        }
        if (penalty.getPenaltyAmount() == null) {
            penalty.setPenaltyAmount(BigDecimal.ZERO);
        }

        if (MeasureUnit.DELAY_WORKING_DAYS.value().equalsIgnoreCase(penalty.getElementInPenalty()) || MeasureUnit.DELAY_CALENDAR_DAYS.value().equalsIgnoreCase(penalty.getElementInPenalty())) {
            penalty.setDelayDaysTim(penalty.getTotalDelay());
        } else if ("opera".equalsIgnoreCase(penalty.getSystemCode())
                && (MeasureUnit.DELAY_CALENDAR_HOURS.value().equalsIgnoreCase(penalty.getElementInPenalty())
                        || MeasureUnit.DELAY_WORKING_HOURS.value().equalsIgnoreCase(penalty.getElementInPenalty()))) {
            penalty.setDelayHoursTim(penalty.getTotalDelay());
        }

    }

    private void mergeAll(final List<PenaltyEntity> penalties) {
        penalties.stream().forEach(p -> penaltyDAO.merge(p));
    }

    private List<Long> prepareGroupIds(final List<PenaltyAggregationEntity> aggregations) {
        List<Long> groupIds = new ArrayList<>();
        aggregations.stream().forEach(p -> groupIds.add(p.getId()));
        return groupIds;
    }

    private void processVertically(final List<PenaltyAggregationEntity> aggregations, final String userId) {
        for (PenaltyAggregationEntity aggregationGroup : aggregations) {
            List<PenaltyEntity> penalties = penaltyDAO.findByAggregationId(aggregationGroup.getId());
            processPenaltiesInSla1(penalties.stream()
                    .filter(p -> p.getInSla1() != null && p.getInSla1().equals("NO")
                            && !("NO".equalsIgnoreCase(p.getFictiveProjectCode()) && p.getProjectCode() != null))
                    .collect(Collectors.toList()), userId, penalties.size());

            processPenaltiesInSla2(penalties.stream()
                    .filter(p -> p.getInSla2() != null && p.getInSla2().equals("NO")
                            && !("NO".equalsIgnoreCase(p.getFictiveProjectCode()) && p.getProjectCode() != null))
                    .collect(Collectors.toList()), userId, penalties.size());

            List<PenaltyEntity> massivePenalties = penaltyDAO.findMassiveByAggregationId(aggregationGroup.getId());
            // penaltyDAO.calculateAverageChargeAmount(aggregationGroup.getId(),
            // userId);
            // penaltyDAO.calculateAverageDelay1(aggregationGroup.getId(),
            // userId);
            // penaltyDAO.calculateAverageDelay2(aggregationGroup.getId(),
            // userId);
            // penaltyDAO.calculateAverageDelay3(aggregationGroup.getId(),
            // userId);
            // penaltyDAO.evaluateMassiveSla100(aggregationGroup.getId(),
            // userId);

            processMassivePenaltiesInSla1(
                    massivePenalties.stream().filter(p -> p.getInSla1() != null && p.getInSla1().equals("NO")).collect(Collectors.toList()),
                    userId, massivePenalties.size());
            processMassivePenaltiesInSla2(
                    massivePenalties.stream().filter(p -> p.getInSla2() != null && p.getInSla2().equals("NO")).collect(Collectors.toList()),
                    userId, massivePenalties.size());

            // penaltyDAO.updateMissingFields(aggregationGroup.getId(), userId);
        }

    }

    private void processVerticallyV2(final List<PenaltyAggregationEntity> aggregations, final String userId, final boolean isRecalculate) {
        for (PenaltyAggregationEntity aggregationGroup : aggregations) {
            log.debug("Started processing aggregation {}", aggregationGroup.getId());
            List<Integer> partitionKeys = getPartitionKeysForAggregation(aggregationGroup);
            Long totalCount = penaltyDAO.getTotalCountForAggregationId(aggregationGroup.getId(), partitionKeys);
            Long totalApprovedCount1 = 0L;
            Long totalApprovedCount2 = 0L;
            if (isRecalculate) {
                totalApprovedCount1 = penaltyDAO.getTotalApprovedCount1ForAggregationId(aggregationGroup.getId(), partitionKeys);
                totalApprovedCount2 = penaltyDAO.getTotalApprovedCount2ForAggregationId(aggregationGroup.getId(), partitionKeys);
            }

            Pair<BigDecimal, BigDecimal> tresholdValues = getSlaTresholdValues(aggregationGroup.getId(), partitionKeys);
            BigDecimal slaTresholdValue1 = tresholdValues.getFirst();
            BigDecimal slaTresholdValue2 = tresholdValues.getSecond();
            long numberOfPenaltyToBeUpdatedForSla1 = calculateNumberOfItemsToBeUpdated(totalCount, slaTresholdValue1.doubleValue())
                    - totalApprovedCount1;
            long numberOfPenaltyToBeUpdatedForSla2 = calculateNumberOfItemsToBeUpdated(totalCount, slaTresholdValue2.doubleValue())
                    - totalApprovedCount2;
            if (numberOfPenaltyToBeUpdatedForSla1 < 0) {
                numberOfPenaltyToBeUpdatedForSla1 = 0;
            }
            if (numberOfPenaltyToBeUpdatedForSla2 < 0) {
                numberOfPenaltyToBeUpdatedForSla2 = 0;
            }

            log.debug("Updating Penalty Typology");
            aggregationGroup.setPenaltyTypology(penaltyDAO.getPenaltyTypology(aggregationGroup.getPenaltyCode()));

            aggregationGroup.setPercentageS1(BigDecimal.valueOf(100).subtract(slaTresholdValue1));
            aggregationGroup.setPercentageS2(BigDecimal.valueOf(100).subtract(slaTresholdValue2));
            log.debug("Total count: {}, #sla1: {}, #sl2: {}", totalCount, numberOfPenaltyToBeUpdatedForSla1,
                    numberOfPenaltyToBeUpdatedForSla2);

            log.debug("Updating F1 Columns");
            penaltyDAO.updateFranchigia1ColumnsV2(aggregationGroup.getId(), numberOfPenaltyToBeUpdatedForSla1, partitionKeys, userId);
            aggregationGroup.setNumberOfInFranchigia1Yes(calculateNumberOfItemsToBeUpdated(totalCount, slaTresholdValue1.doubleValue()));
            log.debug("Updating F2 Columns");
            penaltyDAO.updateFranchigia2ColumnsV2(aggregationGroup.getId(), numberOfPenaltyToBeUpdatedForSla2, partitionKeys, userId);
            aggregationGroup.setNumberOfInFranchigia2Yes(calculateNumberOfItemsToBeUpdated(totalCount, slaTresholdValue2.doubleValue()));
            aggregationGroup.setNumberOfRecords(totalCount);
            log.debug("Massiva 100");
            penaltyDAO.evaluateMassiveSla100(aggregationGroup.getId(), partitionKeys, userId);
            penaltyDAO.evaluateTotalDelayMassive(aggregationGroup.getId(), partitionKeys, userId);
            log.debug("Updating Amounts");
            penaltyDAO.calculateAverageChargeAmount(aggregationGroup.getId(), partitionKeys, userId);
            penaltyDAO.updateAverageChargeAmount(aggregationGroup.getId(), partitionKeys, userId);
            log.debug("Updating Delays");
            penaltyDAO.calculateAverageDelayDays1(aggregationGroup.getId(), partitionKeys, userId);
            penaltyDAO.calculateAverageDelayDays2(aggregationGroup.getId(), partitionKeys, userId);
            penaltyDAO.calculateAverageDelayDays3(aggregationGroup.getId(), partitionKeys, userId);

            penaltyDAO.updateAverageDelayDays1(aggregationGroup.getId(), partitionKeys, userId);
            penaltyDAO.updateAverageDelayDays2(aggregationGroup.getId(), partitionKeys, userId);

            penaltyDAO.penaltyAmountMassive(aggregationGroup.getId(), partitionKeys, userId);

            log.debug("Updating Massive F1 Columns");
            penaltyDAO.updateFranchigia1MassiveColumnsV2(aggregationGroup.getId(), numberOfPenaltyToBeUpdatedForSla1, partitionKeys,
                    userId);
            log.debug("Updating Massive F2 Columns");
            penaltyDAO.updateFranchigia2MassiveColumnsV2(aggregationGroup.getId(), numberOfPenaltyToBeUpdatedForSla2, partitionKeys,
                    userId);
            log.debug("Updating Missing Fields");
            aggregationGroup.setCreatedBy(userId);
            penaltyAggregationDAO.merge(aggregationGroup);
            penaltyDAO.updateMissingFields(aggregationGroup.getId(), partitionKeys, userId);
            log.debug("Updated Missing Fields, flushing and clearing cache...");
            penaltyDAO.flush();
            penaltyDAO.clear();
            log.debug("Finished processing aggregation {}", aggregationGroup.getId());
        }

    }

    private List<Integer> getPartitionKeysForAggregation(final PenaltyAggregationEntity aggregation) {
        FranchigiaPeriodEntity period = franchigiaPeriodDAO.findByPK(aggregation.getFranchigiaPeriodId());
        List<Integer> keys = new ArrayList<>();
        if (period.getFranchigiaFrequency().equals(FrequencyType.YEARLY.getValue())) {
            addKeysForNumberOfMonths(12, period, keys);
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.HALFYEARLY.getValue())) {
            addKeysForNumberOfMonths(6, period, keys);
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.QUARTERLY.getValue())) {
            addKeysForNumberOfMonths(3, period, keys);
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.DAILY.getValue())) {
            keys.add(getPartitonKeyForDate(period.getStartDate()));
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.MONTHLY.getValue())) {
            addKeysForNumberOfMonths(1, period, keys);
        }
        return keys;
    }

    private List<Integer> getPartitionKeysForPeriod(final FranchigiaPeriodEntity period) {
        List<Integer> keys = new ArrayList<>();
        if (period.getFranchigiaFrequency().equals(FrequencyType.YEARLY.getValue())) {
            addKeysForNumberOfMonths(12, period, keys);
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.HALFYEARLY.getValue())) {
            addKeysForNumberOfMonths(6, period, keys);
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.QUARTERLY.getValue())) {
            addKeysForNumberOfMonths(3, period, keys);
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.DAILY.getValue())) {
            keys.add(getPartitonKeyForDate(period.getStartDate()));
        } else if (period.getFranchigiaFrequency().equals(FrequencyType.MONTHLY.getValue())) {
            addKeysForNumberOfMonths(1, period, keys);
        }
        return keys;
    }

    private void addKeysForNumberOfMonths(final int numberOfMonths, final FranchigiaPeriodEntity period, final List<Integer> keys) {
        for (int i = 0; i < numberOfMonths; i++) {
            keys.add(getPartitonKeyForDate(period.getStartDate().plusMonths(i)));
        }
    }

    private Integer getPartitonKeyForDate(final LocalDateTime date) {
        Integer partitionKey = 0;
        if (date.getMonthValue() < 10) {
            partitionKey = Integer.parseInt(String.valueOf(date.getYear()) + "0" + String.valueOf(date.getMonthValue()));
        } else {
            partitionKey = Integer.parseInt(String.valueOf(date.getYear()) + String.valueOf(date.getMonthValue()));
        }
        return partitionKey;
    }

    private Pair<BigDecimal, BigDecimal> getSlaTresholdValues(final Long aggregationId, final List<Integer> partitionKeys) {
        log.debug("Sla treshold values calculation..");
        List<PenaltyEntity> penalties = penaltyDAO.findByAggregationId(aggregationId, partitionKeys, 1, 1);
        log.debug("Retrieved sample");
        if (!CollectionUtils.isEmpty(penalties)) {
            return new Pair<>(
                    penalties.get(0).getSlaThresholdValue1() == null ? BigDecimal.valueOf(100) : penalties.get(0).getSlaThresholdValue1(),
                    penalties.get(0).getSlaThresholdValue2() == null ? BigDecimal.valueOf(100) : penalties.get(0).getSlaThresholdValue2());
        } else {
            return new Pair<>(BigDecimal.valueOf(100), BigDecimal.valueOf(100));
        }
    }

    private BigDecimal calculateAmount(final Long calcRuleId, final PenaltyEntity item) {
        CalculationRuleWithDetail calculationRule = CacheManager.calculationRuleCache.get(calcRuleId);
        item.setChargeAmount(item.getAverageChargeAmount());
        String[] params = calculationRule.getInputParameters().toLowerCase().split(",");

        int matchingDetail = SmsRuleCalculationHelper.findCalculationDetail(item, calculationRule, "0", "1");

        double penaltyAmount = 0D;

        penaltyAmount += SmsRuleCalculationHelper.evaluate(item, calculationRule.getDetails().get(matchingDetail), params, "0", "1");

        return new BigDecimal(penaltyAmount).setScale(4, RoundingMode.HALF_EVEN);
    }

    private void processPenaltiesInSla1(final List<PenaltyEntity> penalties, final String userId, final int size) {

        long numberOfPenaltyToBeUpdated = calculateNumberOfItemsToBeUpdated(size,
                penalties.isEmpty() || penalties.get(0).getSlaThresholdValue1() == null ? 0
                        : penalties.get(0).getSlaThresholdValue1().doubleValue());
        if (!penalties.isEmpty() && penalties.get(0).getSlaThresholdValue1() != null) {
            List<Long> idsToBeUpdated = getPenaltyIdsForUpdate(penalties, numberOfPenaltyToBeUpdated);
            penaltyDAO.updateFranchigia1Columns(idsToBeUpdated, userId);

        }

    }

    private void processMassivePenaltiesInSla1(final List<PenaltyEntity> penalties, final String userId, final int size) {

        long numberOfPenaltyToBeUpdated = calculateNumberOfItemsToBeUpdated(size,
                penalties.isEmpty() || penalties.get(0).getSlaThresholdValue1() == null ? 0
                        : penalties.get(0).getSlaThresholdValue1().doubleValue());
        if (!penalties.isEmpty() && penalties.get(0).getSlaThresholdValue1() != null) {
            List<Long> idsToBeUpdated = getPenaltyIdsForUpdate(penalties, numberOfPenaltyToBeUpdated);
            penaltyDAO.updateFranchigia1MassiveColumns(idsToBeUpdated, userId);

        }

    }

    private void prepareRuleCache() {
        List<CalculationRuleWithDetail> rules = calculationRuleDAO.getCalculationRuels();
        CacheManager.calculationRuleCache.clear();
        for (CalculationRuleWithDetail calculationRuleWithDetail : rules) {
            CacheManager.calculationRuleCache.put(calculationRuleWithDetail.getId(), calculationRuleWithDetail);
        }
        CacheManager.valorizationRuleCache.clear();
        CacheManager.valorizationRuleCache.addAll(valorizationRuleDAO.getRules());
    }

    private void processPenaltiesInSla2(final List<PenaltyEntity> penalties, final String userId, final int size) {
        long numberOfPenaltyToBeUpdated = calculateNumberOfItemsToBeUpdated(size,
                penalties.isEmpty() || penalties.get(0).getSlaThresholdValue2() == null ? 0
                        : penalties.get(0).getSlaThresholdValue2().doubleValue());
        if (!penalties.isEmpty() && penalties.get(0).getSlaThresholdValue2() != null) {
            List<Long> idsToBeUpdated = getPenaltyIdsForUpdate(penalties, numberOfPenaltyToBeUpdated);
            penaltyDAO.updateFranchigia2Columns(idsToBeUpdated, userId);

        }

    }

    private void processMassivePenaltiesInSla2(final List<PenaltyEntity> penalties, final String userId, final int size) {
        long numberOfPenaltyToBeUpdated = calculateNumberOfItemsToBeUpdated(size,
                penalties.isEmpty() || penalties.get(0).getSlaThresholdValue2() == null ? 0
                        : penalties.get(0).getSlaThresholdValue2().doubleValue());
        if (!penalties.isEmpty() && penalties.get(0).getSlaThresholdValue2() != null) {
            List<Long> idsToBeUpdated = getPenaltyIdsForUpdate(penalties, numberOfPenaltyToBeUpdated);
            penaltyDAO.updateMassiveFranchigia2Columns(idsToBeUpdated, userId);

        }

    }

    private long calculateNumberOfItemsToBeUpdated(final long size, final double slaTreshold) {
     // formula (size * (1 - (slaTreshold * 0.01)))
        BigDecimal sizeBD = new BigDecimal(size);
        BigDecimal slaThresholdBD = new BigDecimal(slaTreshold);

        BigDecimal slaPercentage = slaThresholdBD.multiply(new BigDecimal("0.01"));
        log.info("calculateNumberOfItemsToBeUpdated size: {}", size);
        log.info("calculateNumberOfItemsToBeUpdated slaTreshold: {}", slaTreshold);
        log.info("calculateNumberOfItemsToBeUpdated slaPercentage: {}", slaPercentage);

        BigDecimal one = new BigDecimal("1");
        BigDecimal result = sizeBD.multiply(one.subtract(slaPercentage));
        log.info("result: {}", result);
        result = result.setScale(0, RoundingMode.HALF_UP);
        log.info("result rounded: {}", result);

        return result.longValue();
    }

    private List<Long> getPenaltyIdsForUpdate(final List<PenaltyEntity> penalties, final long numberOfPenaltyToBeUpdated) {
        List<Long> idsToBeUpdated = new ArrayList<>();

        int size;
        if (numberOfPenaltyToBeUpdated != 0 && numberOfPenaltyToBeUpdated > penalties.size()) {
            size = penalties.size();
        } else {
            size = (int) numberOfPenaltyToBeUpdated;
        }

        for (int i = 0; i < size; i++) {
            idsToBeUpdated.add(penalties.get(i).getId());
        }
        return idsToBeUpdated;
    }

    private List<PenaltyAggregationEntity> aggregatePenalty(final PenaltyFranchigiaAggregationEntity agg, final Long franchigiaPeriodId,
            final String userId) {
        List<AggregationGroup> aggregatedPenalties = penaltyDAO
                .executeSelectAggregationQuery(generateAggregateSelectQuery(agg, franchigiaPeriodId));
        List<PenaltyAggregationEntity> aggregations = new ArrayList<>();
        for (AggregationGroup group : aggregatedPenalties) {
            PenaltyAggregationEntity aggregationEntity = prepareAndCreatePenaltyAgg(franchigiaPeriodId, agg, userId, group.getValues());
            aggregationEntity.setNumberOfRecords(Long.valueOf(group.getIdsInGroup().size()));
            aggregations.add(aggregationEntity);

            List<List<Long>> smallerLists = Lists.partition(group.getIdsInGroup(), 30000);
            for (List<Long> list : smallerLists) {
                penaltyDAO.updatePenaltiesAggregationId(aggregationEntity.getId(), userId, list);
            }
        }
        return aggregations;
    }

    private List<PenaltyAggregationEntity> aggregatePenaltyWithMap(final PenaltyFranchigiaAggregationEntity agg,
            final Long franchigiaPeriodId, final String userId) {
        Map<String, String> aggColumnsWithTypes = penaltyDAO.getFranchigiaAggregationColumnsWithTypes();
        log.debug("START aggregatePenaltyWithMap for aggregationId {} and franchigiaPeriodId {}", agg.getId(), franchigiaPeriodId);

        log.debug("aggColumnsWithTypes {}", aggColumnsWithTypes);
        List<AggregationGroup> aggregatedPenalties = penaltyDAO
                .executeSelectAggregationQuery(generateAggregateSelectQueryWithMap(agg, franchigiaPeriodId, aggColumnsWithTypes));
        log.debug("aggregatedPenalties {}", aggregatedPenalties);

        List<PenaltyAggregationEntity> aggregations = new ArrayList<>();
        for (AggregationGroup group : aggregatedPenalties) {
            log.debug("aggregatedPenalties GROUP {}", group);
            PenaltyAggregationEntity aggregationEntity = prepareAndCreatePenaltyAgg(franchigiaPeriodId, agg, userId, group.getValues());
            aggregationEntity.setNumberOfRecords(Long.valueOf(group.getIdsInGroup().size()));
            aggregations.add(aggregationEntity);

            List<List<Long>> smallerLists = Lists.partition(group.getIdsInGroup(), 30000);
            for (List<Long> list : smallerLists) {
                penaltyDAO.updatePenaltiesAggregationId(aggregationEntity.getId(), userId, list);
            }
        }
        log.debug("END aggregatePenaltyWithMap for aggregationId {} and franchigiaPeriodId {}", agg.getId(), franchigiaPeriodId);
        return aggregations;
    }

    private List<PenaltyAggregationEntity> aggregatePenaltyV2(final PenaltyFranchigiaAggregationEntity agg,
            final List<Integer> partitionKeys, final Long franchigiaPeriodId, final String userId) {
        log.debug("START aggregatePenaltyV2 for aggregationId {} and franchigiaPeriodId {}", agg.getId(), franchigiaPeriodId);
        String tempTableName = "pnt_tmp_fran_group_" + franchigiaPeriodId + "_" + agg.getId();
        penaltyDAO.executeUpdateQuery(generateCreateTempQuery(agg, franchigiaPeriodId, tempTableName), partitionKeys);

        List<FranchigiaAggregationGroup> aggregatedPenalties = penaltyDAO.executeTempSelectAggregationQuery(tempTableName);

        List<PenaltyAggregationEntity> aggregations = new ArrayList<>();
        for (FranchigiaAggregationGroup group : aggregatedPenalties) {
            PenaltyAggregationEntity aggregationEntity = prepareAndCreatePenaltyAgg(franchigiaPeriodId, agg, userId, group.getValues());
            aggregationEntity.setNumberOfRecords(group.getItemsCount());
            aggregations.add(aggregationEntity);

            penaltyDAO.updatePenaltiesAggregationIdV3(aggregationEntity.getId(), userId, group.getGroupId(), tempTableName);

        }
        penaltyDAO.executeUpdateQuery("DROP TABLE IF EXISTS " + tempTableName);

        log.debug("END aggregatePenaltyV2 for aggregationId {} and franchigiaPeriodId {}", agg.getId(), franchigiaPeriodId);
        return aggregations;
    }

    private String generateAggregateSelectQuery(final PenaltyFranchigiaAggregationEntity agg, final Long franchigiaPeriodId) {
        StringBuilder aggregateSelectQuery = new StringBuilder();
        // @formatter:off
        aggregateSelectQuery.append("select  subquery.penalty_id as penaltyId,cast(subquery.ids as text) as ids, concat_ws(',', ")
                            .append(generateSubQueryForValues(agg.getAggregationCriteria()))
                            .append(") as values ")
                            .append("from ( select ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id as penalty_id,array_agg(id) as ids from pnt_penalty where")
                            .append(" penalty_id = "  + agg.getPenaltyId())
                            .append(" and franchigia_period_id = "+franchigiaPeriodId +" and franchigia_flag = '1'  and status in ( 'Active', 'InReconciliation') and flag_massive_1 != 'Y' ")
                            .append((agg.getScope() != null ? (" and scope = '" + agg.getScope() + "' ") : ""))
                            .append(" group by ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id ")
                            .append(" ) as subquery");
        // @formatter:on
        return aggregateSelectQuery.toString();
    }

    private String generateAggregateSelectQueryWithMap(final PenaltyFranchigiaAggregationEntity agg, final Long franchigiaPeriodId,
    final Map<String, String> aggColumnsWithTypes) {
        StringBuilder aggregateSelectQuery = new StringBuilder();
        String[] columns = StringUtils.split(agg.getAggregationCriteria(), ",");

        List<String> groupByColumns = new ArrayList<>();
        for (String columnName : aggColumnsWithTypes.keySet()) {
            String columnValue = aggColumnsWithTypes.get(columnName);
            if ("timestamp".equals(columnValue) || "date".equals(columnValue)) {
                if (Arrays.asList(columns).contains(columnName)) {
                    aggColumnsWithTypes.put(columnName, "coalesce(to_char(" + columnName + ",'YYYY-MM-DD'),'null')");
                    groupByColumns.add(columnName);
                }
            } else if ("constant".equals(columnValue)) {
                if (Arrays.asList(columns).contains(columnName)) {
                    String alias = toSnakeCase(columnName);

                    groupByColumns.add(alias);
                    aggColumnsWithTypes.put(columnName, "'" + columnName + "' as " + alias);
                }
            } else {
                groupByColumns.add(columnName);
                aggColumnsWithTypes.put(columnName, columnName);
            }
        }

        List<String> selectColumns = new ArrayList<>();
        for (String column : columns) {
            selectColumns.add(aggColumnsWithTypes.get(column));
        }

        // @formatter:off
        aggregateSelectQuery.append("select  subquery.penalty_id as penaltyId,cast(subquery.ids as text) as ids, concat_ws(',', ")
                            .append(generateSubQueryForValues(agg.getAggregationCriteria()))
                            .append(") as values ")
                            .append("from ( select ")
                            .append(String.join(", ", selectColumns))
                            .append(",penalty_id as penalty_id,array_agg(id) as ids from pnt_penalty where")
                            .append(" penalty_id = "  + agg.getPenaltyId())
                            .append(" and franchigia_period_id = "+franchigiaPeriodId +" and franchigia_flag = '1'  and status in ( 'Active', 'InReconciliation') and flag_massive_1 != 'Y' ")
                            .append((agg.getScope() != null ? (" and scope = '" + agg.getScope() + "' ") : ""))
                            .append(" group by ")
                            .append(String.join(", ", groupByColumns))
                            .append(",penalty_id ")
                            .append(" ) as subquery");
        // @formatter:on
        return aggregateSelectQuery.toString();
        }

        public static String toSnakeCase(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String sanitizedInput = input.replaceAll("[()]", "");
        String[] parts = sanitizedInput.split("[\\s,]+");
        StringBuilder sBuilder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i != 0) {
                sBuilder.append("_");
            }
            String word = parts[i];
            StringBuilder wordBuilder = new StringBuilder();
            for (char c : word.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    wordBuilder.append("_").append(Character.toLowerCase(c));
                } else {
                    wordBuilder.append(c);
                }
            }
            sBuilder.append(wordBuilder.toString().replaceAll("^_", ""));
        }

        return sBuilder.toString();
    }


    private String generateAggregateSelectQueryV2(final PenaltyFranchigiaAggregationEntity agg, final Long franchigiaPeriodId) {
        StringBuilder aggregateSelectQuery = new StringBuilder();
        // @formatter:off
        aggregateSelectQuery.append("select  subquery.penalty_id as penaltyId,cast(subquery.ids as text) as ids, concat_ws(',', ")
                            .append(generateSubQueryForValues(agg.getAggregationCriteria()))
                            .append(") as values ")
                            .append("from ( select ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id as penalty_id,array_agg(id) as ids from pnt_penalty where")
                            .append(" year_month_partkey in :keys and penalty_id = "  + agg.getPenaltyId())
                            .append(" and franchigia_period_id = "+franchigiaPeriodId +" and franchigia_flag = '1'  and status in ( 'Active', 'InReconciliation') and flag_massive_1 != 'Y' ")
                            .append((agg.getScope() != null ? (" and scope = '" + agg.getScope() + "' ") : ""))
                            .append(" group by ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id ")
                            .append(" ) as subquery");
        // @formatter:on
        return aggregateSelectQuery.toString();
    }

    private String generateCreateTempQuery(final PenaltyFranchigiaAggregationEntity agg, final Long franchigiaPeriodId,
            final String tempTableName) {
        StringBuilder aggregateSelectQuery = new StringBuilder();

        // @formatter:off
        aggregateSelectQuery.append("DROP TABLE IF EXISTS ").append(tempTableName).append("; ")
                            .append("create unlogged table ").append(tempTableName).append(" as ")
                            .append("select year_month_partkey as pnt_partition, dense_rank () over(order by ")
                            .append(agg.getAggregationCriteria()).append(") as groupId,")
                            .append("penalty_id as penaltyId, concat_ws(',', ")
                            .append(generateSubQueryForValues(agg.getAggregationCriteria()))
                            .append(") as values, ")
                            .append("array_agg(id) as ids from pnt_penalty where")
                            .append(" year_month_partkey in :keys and penalty_id = "  + agg.getPenaltyId())
                            .append(" and franchigia_period_id = "+franchigiaPeriodId +" and franchigia_flag = '1'  and status in ( 'Active', 'InReconciliation') and flag_massive_1 != 'Y' ")
                            .append((agg.getScope() != null ? (" and scope = '" + agg.getScope() + "' ") : ""))
                            .append(" group by ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id,year_month_partkey ");
        // @formatter:on
        return aggregateSelectQuery.toString();
    }

    private String generateAggregateSelectQueryForSingleItem(final PenaltyAggregationEntity agg, final Long franchigiaPeriodId,
            final Long id) {
        StringBuilder aggregateSelectQuery = new StringBuilder();
        // @formatter:off
        aggregateSelectQuery.append("select  subquery.penalty_id as penaltyId,cast(subquery.ids as text) as ids, concat_ws(',', ")
                            .append(generateSubQueryForValues(agg.getAggregationCriteria()))
                            .append(") as values ")
                            .append("from ( select ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id as penalty_id,array_agg(id) as ids from pnt_penalty where")
                            .append(" penalty_id = "  + agg.getPenaltyId())
                            .append(" and id  = "  + id)
                            .append(" and franchigia_period_id = "+franchigiaPeriodId +" and franchigia_flag = '1'  and status = 'Active' and flag_massive_1 != 'Y' ")
                            .append(" group by ")
                            .append(agg.getAggregationCriteria())
                            .append(",penalty_id ")
                            .append(" ) as subquery");
        // @formatter:on
        return aggregateSelectQuery.toString();
    }

    private String generateSubQueryForValues(final String aggregationCriteria) {
        String[] parts = aggregationCriteria.split(",");
        StringBuilder sBuilder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i != 0) {
                sBuilder.append(" , ");
            }
            sBuilder.append("coalesce(cast('").append(parts[i]).append("' as text), 'null')");

        }
        return sBuilder.toString();
    }

    private PenaltyAggregationEntity prepareAndCreatePenaltyAgg(final Long franchigiaPeriodId, final PenaltyFranchigiaAggregationEntity agg,
            final String userId, final String values) {
        log.debug("START prepareAndCreatePenaltyAgg for aggregationId {} and franchigiaPeriodId {} with values {}", agg.getId(), franchigiaPeriodId, values);

        PenaltyAggregationEntity aggEntity = new PenaltyAggregationEntity();
        aggEntity.setAggregationCriteria(agg.getAggregationCriteria());
        aggEntity.setCreated(LocalDateTime.now());
        aggEntity.setCreatedBy(userId);
        aggEntity.setFranchigiaPeriodId(franchigiaPeriodId);
        aggEntity.setPenaltyId(agg.getPenaltyId());
        log.debug("aggEntity before penaltyEntity {}", aggEntity);

        RegistryPenaltyEntity penaltyEntity = registryPenaltyDAO.findByPK(agg.getPenaltyId());

        aggEntity.setPenaltyCode(penaltyEntity.getCode());
        aggEntity.setPenaltyName(penaltyEntity.getPenaltyName());
        aggEntity.setStatus(AggregationStatus.ACTIVE.value());
        aggEntity.setValues(values);
        aggEntity = penaltyAggregationDAO.persist(aggEntity);
        log.debug("penaltyEntity {}", penaltyEntity);
        log.debug("aggEntity after penaltyEntity {}", aggEntity);

        log.debug("END prepareAndCreatePenaltyAgg for aggregationId {} and franchigiaPeriodId {} with values {}", agg.getId(),
                franchigiaPeriodId, values);
        return aggEntity;
    }

    @Override
    public void closePeriodByProcess(final EmptyRequest request) throws ApiException {
        List<Long> franchigiaPeriodIds = franchigiaPeriodDAO.getPeriodIdsForClose();

        log.info("closePeriodByProcess: {}", franchigiaPeriodIds);

        for (Long id : franchigiaPeriodIds) {
            HandlePeriodRequest request2 = new HandlePeriodRequest(null, id, true);
            EntityRequest<HandlePeriodRequest> request3 = new EntityRequest<>();
            request3.setEntity(request2);
            handleFranchigiaPeriodByFileHeadIdv2(request3);
        }

        // List<PenaltyFranchigiaAggregationEntity> aggCriteriaList =
        // penaltyFranchigiaAggregationDAO.getAllActive();
        for (Long id : franchigiaPeriodIds) {

            List<PenaltyAggregationEntity> aggregations = penaltyAggregationDAO.getPeriodAggregations(id);
            log.debug("Vertical processing");
            processVertically(aggregations, request.getUserId());
            log.debug("Horizonatal processing");
            processHorizontally(prepareGroupIds(aggregations), request.getUserId());
            log.debug("Updating aggregations");
            penaltyAggregationDAO.updateStatusofAggregations(
                    aggregations.stream().map(PenaltyAggregationEntity::getId).collect(Collectors.toList()), AggregationStatus.PROCESSED,
                    request.getUserId());

            franchigiaPeriodDAO.closePeriod(request.getUserId(), id);
        }
    }

    @Override
    public void rollbackFranchigiaPeriod(final EntityRequest<Long> request) throws ApiException {
        penaltyDAO.rollbackFranchigiaPeriod(request.getEntity(), request.getUserId());

        FranchigiaPeriodEntity entity = franchigiaPeriodDAO.findByPK(request.getEntity());
        entity.setModified(LocalDateTime.now());
        entity.setModifiedBy(request.getUserId());
        entity.setStatus(RegistryPeriodStatus.OPEN.value());

        franchigiaPeriodDAO.merge(entity);

    }

    @Override
    public void candidateAggregationCriteria(final EntityRequest<Long> request) throws ApiException {
        franchigiaPeriodRequestValidator.validateExistsFranchigiaPeriod(request, "validateNumber");
        FranchigiaPeriodEntity franchigiaPeriod = franchigiaPeriodDAO.getFranchigiaPeriod(request.getEntity());
        prepareRuleCache();
        if (FrequencyType.YEARLY.getValue().equalsIgnoreCase(franchigiaPeriod.getFranchigiaFrequency())) {
            handleAnnualErrato(franchigiaPeriod.getEndDate(), request);
        }
        List<PenaltyFranchigiaAggregationEntity> queueDataList = penaltyFranchigiaAggregationDAO.getAllActive();
        // @formatter:off
        List<QueueRequest> queues = new ArrayList<>();
        queues.add(prepareQueue(null, franchigiaPeriod.getId()));
        // @formatter:on
        log.debug("Sending {} criteria candidates to JO");
        joctopusSetupClientService.create(queues);
    }

    private QueueRequest prepareQueue(final PenaltyFranchigiaAggregationEntity criteria, final Long periodId) {
        QueueRequest queueRequest = new QueueRequest();
        queueRequest.setName(queueName);
        queueRequest.setTaskId(taskId);
        queueRequest.setSeqno(1L);
        queueRequest.setParams(createParams(criteria, periodId));
        return queueRequest;
    }

    private List<QueueParamRequest> createParams(final PenaltyFranchigiaAggregationEntity criteria, final Long periodId) {
        List<QueueParamRequest> params = new ArrayList<>();
        params.add(createParamForPeriod(periodId));
        params.add(createParamForAggregation(criteria));
        return params;
    }

    private static QueueParamRequest createParamForPeriod(final Long periodId) {
        QueueParamRequest param = new QueueParamRequest();
        param.setName("periodId");
        param.setValue(periodId.toString());
        param.setSeqno(1L);
        return param;
    }

    private static QueueParamRequest createParamForAggregation(final PenaltyFranchigiaAggregationEntity criteria) {
        QueueParamRequest param = new QueueParamRequest();
        param.setName("aggregationId");
        param.setValue("0");
        param.setSeqno(2L);
        return param;
    }

    @Override
    public void processAggregationCriteria(final EntityRequest<ProcessAggregationCriteriaRequest> request) throws ApiException {
        requestValidator.validate(request);
        // PenaltyFranchigiaAggregationEntity agg =
        // penaltyFranchigiaAggregationDAO.findByPK(request.getEntity().getAggregationId());
        FranchigiaPeriodEntity franchigiaPeriod = franchigiaPeriodDAO.getFranchigiaPeriod(request.getEntity().getPeriodId());
        List<Integer> partitionKeys = getPartitionKeysForPeriod(franchigiaPeriod);
        HandlePeriodRequest request2 = new HandlePeriodRequest(null, franchigiaPeriod.getId(), true);
        EntityRequest<HandlePeriodRequest> request3 = new EntityRequest<>();
        request3.setEntity(request2);
        handleFranchigiaPeriodByFileHeadIdv2(request3);
        // log.debug("Creating aggregations for criteria {}", agg.getId());
        // List<PenaltyAggregationEntity> aggregations = aggregatePenaltyV2(agg,
        // partitionKeys, request.getEntity().getPeriodId(),
        // request.getUserId());
        List<PenaltyAggregationEntity> aggregations = penaltyAggregationDAO.getPeriodAggregations(franchigiaPeriod.getId());
        // log.debug("Created {} aggregations for criteria {}",
        // aggregations.size(), agg.getId());
        log.debug("Vertical processing");
        processVerticallyV2(aggregations, request.getUserId(), false);
        log.debug("Horizontal processing");
        processHorizontallyV2(prepareGroupIds(aggregations), getPartitionKeys(aggregations), request.getUserId());
        log.debug("Updating aggregations");
        penaltyAggregationDAO.updateStatusofAggregations(
                aggregations.stream().map(PenaltyAggregationEntity::getId).collect(Collectors.toList()), AggregationStatus.PROCESSED,
                request.getUserId());
        log.debug("Updated aggregations");

        if (!CollectionUtils.isEmpty(partitionKeys)) {

            try {
                penaltyCandidateService.insertAll(new EntityRequest<>(new HashSet<>(partitionKeys)));
            } catch (ApiException e) {
                LOGGER.error("Error during candidate insert: {}", e.getMessage(), e);
            }

        }
    }

    @Override
    public void closePeriodAfterProcessing(final EntityRequest<Long> request) throws ApiException {
        franchigiaPeriodRequestValidator.validateExistsFranchigiaPeriod(request, "validateNumber");
        franchigiaPeriodDAO.closePeriod(request.getUserId(), request.getEntity());
    }

    @Override
    @Transactional
    public void handleFranchigiaPeriodByFileHeadIdv2(final EntityRequest<HandlePeriodRequest> request) throws ApiException {
        log.info("handleFranchigiaPeriodByFileHeadIdv2 START ALL fileHeadIds");
        boolean isPeriodClosing = request.getEntity().isCloseProcess();
        List<Integer> partitionKeys = new ArrayList<>();
        if (request.getEntity().getFranchigiaPeriodId() != null) {
            partitionKeys = getPartitionKeysForPeriod(franchigiaPeriodDAO.findByPK(request.getEntity().getFranchigiaPeriodId()));
        }
        Map<String, String> aggColumnsWithTypes = penaltyDAO.getFranchigiaAggregationColumnsWithTypes();
        for (String columnName : aggColumnsWithTypes.keySet()) {
            String columnValue = aggColumnsWithTypes.get(columnName);
            if ("timestamp".equals(columnValue) || "date".equals(columnValue)) {
                aggColumnsWithTypes.put(columnName, "coalesce(to_char(" + columnName + ",'YYYY-MM-DD'),'null')");
            } else if ("constant".equals(columnValue)) {
                aggColumnsWithTypes.put(columnName, "'" + columnName + "'");
            } else {
                aggColumnsWithTypes.put(columnName, "coalesce(" + columnName + ",'null')");
            }
        }
        StringBuilder sb = new StringBuilder("create unlogged table pnt_aggregations_tmp as with penalties as ( "
                + "select pp.id, penalty_code in ('13','14') and pp.reset_indicator = '1' not_telecomitalia, "
                + " pp.year_month_partkey, pp.penalty_id, pp.franchigia_period_id, pp.penalty_code, ");

        sb.append("case ");
        // List<PenaltyFranchigiaAggregationEntity> allFranchigiaAggregations =
        // penaltyFranchigiaAggregationDAO.getAllActive();
        List<PenaltyFranchigiaVariantAggregation> franchigiaAggregations = franchigiaPeriodDAO.getFranchigiaAggregationVariants();
        Long longOne = 1L; // prod153 PBI
        for (PenaltyFranchigiaVariantAggregation agg : franchigiaAggregations) {
            String[] columns = StringUtils.split(agg.getAggregationCriteria(), ",");
            List<String> selectColumns = new ArrayList<>();
            for (String column : columns) {
                selectColumns.add(aggColumnsWithTypes.get(column));
            }

            String sqlCriteriaString = "array[" + StringUtils.join(selectColumns, " || ',' || ") + ", '" + agg.getId() + "']";
            if (longOne.equals(agg.getVariantCount())) {
                sb.append("when penalty_id = ").append(agg.getPenaltyId()).append(" ");
                if (agg.getScope() != null) {
                    sb.append("and scope = '").append(agg.getScope()).append("' ");
                }
                sb.append("then ").append(sqlCriteriaString);
            } else {
                if (longOne.equals(agg.getVariantNo())) {
                    sb.append("when penalty_id = ").append(agg.getPenaltyId()).append(" ");
                    if (agg.getScope() != null) {
                        sb.append("and scope = '").append(agg.getScope()).append("' ");
                    }
                    sb.append(" then case when ").append(agg.getVariantCondition()).append(" then " + sqlCriteriaString);
                } else if (agg.getVariantNo().equals(agg.getVariantCount())) {
                    sb.append(" else ").append(sqlCriteriaString + " end ");

                } else {
                    sb.append(" when ").append(agg.getVariantCondition()).append(" then " + sqlCriteriaString);
                }
            }

        }
        sb.append(" end vals from pnt_penalty pp  ");
        if (request.getEntity().getFranchigiaPeriodId() == null) {
            sb.append("join PNT_MNL_PROCESS_LOAD tmp on (tmp.reconciliation_key = pp.reconciliation_key) ");
        }

        if (!request.getEntity().isCloseProcess()) {
            sb.append(" where franchigia_period_closed_flag = '1' ");
        }
        if (request.getEntity().getFranchigiaPeriodId() != null) {
            String partitions = partitionKeys.stream().map(String::valueOf).collect(Collectors.joining(","));
            if (!request.getEntity().isCloseProcess()) {
                sb.append(" and pp.franchigia_period_id =  " + request.getEntity().getFranchigiaPeriodId()
                        + " and pp.year_month_partkey in (" + partitions + ")");
            } else {
                sb.append(" where pp.franchigia_period_id =  " + request.getEntity().getFranchigiaPeriodId()
                        + " and pp.year_month_partkey in (" + partitions + ")");
            }

        }
        sb.append(
                "), nontelcoitaly as ( INSERT INTO billing.pnt_non_telco_italy_tmp select id, year_month_partkey from penalties where not_telecomitalia returning id ), "
                        + "grouped_values as ( "
                        + "select count(*) cnt, array_agg(distinct year_month_partkey) partitions, array_agg(id) ids, penalty_id, franchigia_period_id, penalty_code, vals [1] vals , max(vals [2]\\:\\:bigint) ag "
                        + "from penalties where not not_telecomitalia "
                        + "group by penalty_id, franchigia_period_id, penalty_code, vals [1])  "
                        + "select g.penalty_id, g.franchigia_period_id, g.penalty_code, g.vals , coalesce(pa.id, nextval('pnt_penalty_aggregation_seq')) agg_id, "
                        + "case when pa.id is null then true else false end is_new, ids, partitions, f.aggregation_criteria ag "
                        + " from grouped_values g join pnr_penalty_franchigia_aggregation f on (f.id = ag) "
                        + "left join pnt_penalty_aggregation pa " + "on (pa.penalty_id = g.penalty_id "
                        + "and pa.franchigia_period_id = g.franchigia_period_id and pa.penalty_code = g.penalty_code and pa.values = g.vals) ");
        log.info("handleFranchigiaSQL: " + sb.toString());
        log.info("START franchigiaPeriodDAO.createTmpNonTelItaly");
        franchigiaPeriodDAO.createTmpNonTelItaly();
        log.info("END franchigiaPeriodDAO.createTmpNonTelItaly");
        log.info("START franchigiaPeriodDAO.createTmpPntAggTable");
        franchigiaPeriodDAO.createTmpPntAggTable(sb);
        log.info("END franchigiaPeriodDAO.createTmpPntAggTable");
        List<PenaltyAggregationTmp> penaltyAggregationTmps = franchigiaPeriodDAO.getPenaltyAggregationTmps();
        for (PenaltyAggregationTmp aggTmp : penaltyAggregationTmps) {
            Integer updatedPenaltiesCount = penaltyDAO.updatePenaltiesAggIdForPeriod(aggTmp.getAggId(), aggTmp.getPartitionKeys(),
                    request.getEntity().isCloseProcess());
            log.info("updatedPenaltiesCount for aggTmp:{}, penalties count:{}", aggTmp.getAggId(), updatedPenaltiesCount);
        }
        log.info("START updatedAggragations");
        boolean updateFlag = request.getEntity().getFranchigiaPeriodId() == null;
        Integer updatedAggragations = franchigiaPeriodDAO.updateAggregationsUsingPreparedData(updateFlag);
        log.info("END updatedAggragations count:{}", updatedAggragations);
        log.info("START createdAggragations ");
        Integer createdAggragations = franchigiaPeriodDAO.createAggregationsUsingPreparedData(updateFlag);
        log.info("createdAggragations count:{}", createdAggragations);
        log.info("START updatedNonTelPenaltiesCount ");
        Integer updatedNonTelPenaltiesCount = penaltyDAO.updateNonTelItalyPenaltiesUsingPreparedData();
        log.info("updatedNonTelPenaltiesCount count:{}", updatedNonTelPenaltiesCount);
        log.info("handleFranchigiaPeriodByFileHeadIdv2 END ALL fileHeadIds");
    }

    private void updatePenaltyTotalDelayAfterProcessing(final String userId, final PenaltyEntity penalty) {
        if ("SI".equals(penalty.getInFranchigia1()) && penalty.getPenaltyAmount() != null
                && penalty.getPenaltyAmount().compareTo(BigDecimal.ZERO) == 0
                && (penalty.getPenaltyCode() != null && ("13".equals(penalty.getPenaltyCode()) || "14".equals(penalty.getPenaltyCode())))) {
            penalty.setModified(LocalDateTime.now());
            penalty.setModifiedBy(userId);
            penalty.setTotalDelay(BigDecimal.ZERO);
            if (MeasureUnit.DELAY_WORKING_DAYS.value().equalsIgnoreCase(penalty.getElementInPenalty())
                    || MeasureUnit.DELAY_CALENDAR_DAYS.value().equalsIgnoreCase(penalty.getElementInPenalty())) {
                penalty.setDelayDaysTim(penalty.getTotalDelay());
            } else if (MeasureUnit.DELAY_CALENDAR_HOURS.value().equalsIgnoreCase(penalty.getElementInPenalty())
                    || MeasureUnit.DELAY_WORKING_HOURS.value().equalsIgnoreCase(penalty.getElementInPenalty())) {
                penalty.setDelayHoursTim(penalty.getTotalDelay());
            }
        }
    }

}