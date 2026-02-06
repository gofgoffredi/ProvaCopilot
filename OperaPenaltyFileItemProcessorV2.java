package ba.com.zira.billing.penalty.core.impl.fileload;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import ba.com.zira.billing.penalty.api.PenaltyCandidateService;
import ba.com.zira.billing.penalty.api.external.model.FileItemProcessingInfo;
import ba.com.zira.billing.penalty.api.external.model.ValidationRule;
import ba.com.zira.billing.penalty.api.model.AccountTranscodeSourceCode;
import ba.com.zira.billing.penalty.api.model.RegistryResolutionBasic;
import ba.com.zira.billing.penalty.api.model.RetrivePeriodIdResponse;
import ba.com.zira.billing.penalty.api.model.ServiceTypeTranscodeJpubRequest;
import ba.com.zira.billing.penalty.api.model.enums.AnalaysisStatus;
import ba.com.zira.billing.penalty.api.model.enums.FileLoadItemsPenaltyStatus;
import ba.com.zira.billing.penalty.api.model.enums.InSlaTypes;
import ba.com.zira.billing.penalty.api.model.enums.LoadPenaltyScopeType;
import ba.com.zira.billing.penalty.api.model.enums.MeasureUnit;
import ba.com.zira.billing.penalty.api.model.enums.PartitionKeys;
import ba.com.zira.billing.penalty.api.model.enums.PenaltyStatus;
import ba.com.zira.billing.penalty.api.model.franchigia.FranchigiaPeriodResponse;
import ba.com.zira.billing.penalty.api.model.jpub.RequestJpubPrice;
import ba.com.zira.billing.penalty.core.impl.TranslationDateValidator;
import ba.com.zira.billing.penalty.core.utils.CacheManager;
import ba.com.zira.billing.penalty.core.utils.DateUtil;
import ba.com.zira.billing.penalty.dao.ChargeAmountConfigurationDAO;
import ba.com.zira.billing.penalty.dao.FileLoadHeadPenaltyDAO;
import ba.com.zira.billing.penalty.dao.OperaAssuranceLoadPenaltyDAO;
import ba.com.zira.billing.penalty.dao.PenaltyDAO;
import ba.com.zira.billing.penalty.dao.PenaltyResetSetupDAO;
import ba.com.zira.billing.penalty.dao.RegistryPenaltyDAO;
import ba.com.zira.billing.penalty.dao.RegistryPeriodPenaltyDAO;
import ba.com.zira.billing.penalty.dao.RegistryResolutionDAO;
import ba.com.zira.billing.penalty.dao.ServiceTypeTranscodeJpubDAO;
import ba.com.zira.billing.penalty.dao.franchigia.FranchigiaPeriodDAO;
import ba.com.zira.billing.penalty.dao.model.OperaAssuranceLoadPenaltyEntity;
import ba.com.zira.billing.penalty.dao.model.PenaltyEntity;
import ba.com.zira.billing.penalty.dao.model.PenaltyResetSetupEntity;
import ba.com.zira.billing.penalty.dao.model.RegistryPenaltyEntity;
import ba.com.zira.billing.penalty.dao.model.ServiceTypeTranscodeJpubEntity;
import ba.com.zira.billing.penalty.dao.model.custom.ValorizationRuleDetailBasic;
import ba.com.zira.billing.penalty.dao.model.custom.ValorizationRuleWithDetail;
import ba.com.zira.billing.penalty.dao.reconcilation.PenaltyMatchedItemDAO;
import ba.com.zira.commons.exception.ApiException;
import ba.com.zira.commons.message.request.EntityRequest;
import ba.com.zira.commons.model.enums.Indicator;
import ba.com.zira.i18n.MessageTranslator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Component
@Slf4j
public class OperaPenaltyFileItemProcessorV2 implements FileItemProcessorV2<OperaAssuranceLoadPenaltyEntity> {
	private static final Logger LOGGER = LoggerFactory.getLogger(OperaPenaltyFileItemProcessorV2.class);

	private OperaAssuranceLoadPenaltyDAO operaAssuranceLoadPenaltyDAO;
	private FileLoadHeadPenaltyDAO fileLoadHeadPenaltyDAO;
	private PenaltyDAO penaltyDAO;
	private TranslationDateValidator translationDateValidator;
	private RegistryResolutionDAO registryResolutionDAO;
	private RegistryPeriodPenaltyDAO registryPeriodPenaltyDAO;
	private FranchigiaPeriodDAO franchigiaPeriodDAO;
	private FranchigiaAggregator franchigiaAggregator;
	private ServiceTypeTranscodeJpubDAO serviceTypeTranscodeJpubDAO;
	private ChargeAmountConfigurationDAO chargeAmountConfigurationDAO;
	private JpubHandler jpubHandler;
	private PenaltyCandidateService penaltyCandidateService;
	private PenaltyResetSetupDAO penaltyResetSetupDAO;
	private RegistryPenaltyDAO registryPenaltyDAO;
	private PenaltyMatchedItemDAO penaltyMatchedItemDAO;
	private final MessageTranslator messageTranslator;

	@Override
	public void process(final List<FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity>> fileItems,
			final Long processId) {
		LOGGER.debug("Started processing opera penalties : number of penalties in batch {}", fileItems.size());
		LocalDateTime date = LocalDateTime.now();
		String sourcePenaltyTypeCode = fileItems.get(0).getItem().getSourcePenaltyTypeCode();
		Map<String, List<PenaltyEntity>> penaltyMap = penaltyDAO.getByCodeTTs(getCodeTts(fileItems),
				sourcePenaltyTypeCode);
		LOGGER.debug("Started processing opera penalties :  penalties in batch {}", penaltyMap);

		CompletableFuture<List<ServiceTypeTranscodeJpubEntity>> regulatedJpubMap = CompletableFuture
				.supplyAsync(this::getOperaServiceTypeJpubRegulatedMap);
		CompletableFuture<List<ServiceTypeTranscodeJpubEntity>> nonRegulatedJpubMap = CompletableFuture
				.supplyAsync(this::getOperaServiceTypeJpubNonRegulatedMap);

		Map<String, RegistryResolutionBasic> resolutionMap = registryResolutionDAO.getResolutionMap();
		Map<String, List<FranchigiaPeriodResponse>> franchigiaPeriodsMap = franchigiaPeriodDAO.getFranchigiaPeriodId();
		Map<String, List<RetrivePeriodIdResponse>> penaltyPeriodsMap = registryPeriodPenaltyDAO
				.getPeriodPenaltyPeriodIdList();
		List<PenaltyEntity> penalties = new ArrayList<>();
		List<RequestJpubPrice> jpubRequests = new ArrayList<>();
		List<PenaltyResetSetupEntity> rules = penaltyResetSetupDAO.findAllActive("OPERA");
		log.info("process void from OperaPenaltyFileItemProcessorV2 -> process {}, {}, {}, {}", penaltyMap, penalties,
				rules, fileItems);
		fileItems.forEach(i -> process(i, penaltyMap, penalties, regulatedJpubMap, nonRegulatedJpubMap, resolutionMap,
				franchigiaPeriodsMap, penaltyPeriodsMap, jpubRequests, rules));

		// PEN-PROD-172
		boolean flagManualLoadUser = false;
		String manualLoadUser = "system";
		Long manualLoadUserId = 0L;
		// PEN-PROD-172

		for (FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem : fileItems) {
			if (!fileItem.isValid()) {
				fileItem.getItem().setStatus(FileLoadItemsPenaltyStatus.ERROR.value());
				fileItem.getItem().setErrorCode(fileItem.getErrorCode());
				fileItem.getItem().setErrorDescription(fileItem.getErrorMessage());
			}
			fileItem.getItem().setModified(date);
			// PEN-PROD-172
			if (fileItem.getItem().getManualUpdateInd() != null) {
				if (fileItem.getItem().getManualLoadUser() == null) {
					fileItem.getItem().setManualLoadUser(fileItem.getItem().getModifiedBy());
				}
				flagManualLoadUser = true;
				manualLoadUser = fileItem.getItem().getManualLoadUser();
				manualLoadUserId = fileItem.getItem().getFileLoadHeadId();
				log.debug("setManualLoadUser {} for fileItem {} ", fileItem.getItem().getManualLoadUser(),
						fileItem.getItem().getId());
			}

		}
		LOGGER.debug("Persisting chanegs to database");
		operaAssuranceLoadPenaltyDAO.updateLoadItems(fileItems.stream()
				.map(FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity>::getItem).collect(Collectors.toList()));
		handleFranchigiaFlagForTelecomItaly(penalties);
		handlePenaltyTypology(fileItems.stream().map(FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity>::getItem)
				.collect(Collectors.toList()), penalties);

		penaltyDAO.updateOrInsertPenalties(penalties);
		updateToNewPartitionKeys(penalties);
		jpubHandler.handleJpub(jpubRequests);
		persistCandidates(fileItems);
		// PEN-PROD-172
		if (flagManualLoadUser) {
			log.debug("Uploading  manual_load_user {} in pnt_file_load_head for id {} ", manualLoadUser,
					manualLoadUserId);
			fileLoadHeadPenaltyDAO.updateManualLoadUser(manualLoadUserId, manualLoadUser);
		}
		// PEN-PROD-172
		LOGGER.debug("End processing opera penalties : number of penalties in batch {}", fileItems.size());
	}

	private void handleFranchigiaFlagForTelecomItaly(final List<PenaltyEntity> penalties) {
		LOGGER.debug("START: handleFranchigiaFlagForTelecomItaly");
		for (PenaltyEntity penaltyEntity : penalties) {
			if (("13".equals(penaltyEntity.getPenaltyCode()) || "14".equals(penaltyEntity.getPenaltyCode()))
					&& !"Telecom Italia".equals(penaltyEntity.getOaoClosureTtDesc())) {

				// PEN-PROD-226 - update pntPenaltyAggregation
				if (penaltyEntity.getFranchigiaFlag() != null
						&& "1".equalsIgnoreCase(penaltyEntity.getFranchigiaFlag())) {
					LOGGER.debug("Franchigia Flag before reset [{}]", penaltyEntity.getFranchigiaFlag());
					if (penaltyEntity.getAggregationId() != null) {
						Long aggregationId = penaltyEntity.getAggregationId();
						franchigiaPeriodDAO.updateAggregationsUsingAggregationId(aggregationId);
						LOGGER.debug("Updated aggregationId [{}]", aggregationId);
					}
				}
				LOGGER.debug("handleFranchigiaFlagForTelecomItaly - Reset Franchigia Flag=0");
				penaltyEntity.setFranchigiaFlag("0");
				penaltyEntity.setAggregationId(null);
				penaltyEntity.setSlaThresholdValue(new BigDecimal(100));
				penaltyEntity.setPenaltyAmount(new BigDecimal(0));
				// PEN-PROD-226-B02 added reset
				penaltyEntity.setInFranchigia1(null);
				penaltyEntity.setInFranchigia2(null);
				penaltyEntity.setInFranchigia3(null);
				penaltyEntity.setFranchigiaPeriodId(null);
				penaltyEntity.setFranchigiaPeriodClosedFlag(null);
				penaltyEntity.setFranchigiaFrequency(null);

			}

		}

		LOGGER.debug("END: handleFranchigiaFlagForTelecomItaly");

	}

	private void persistCandidates(final List<FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity>> fileItems) {
		HashSet<Integer> set = new HashSet<>();
		for (FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem : fileItems) {
			set.add(fileItem.getPartitionKey());
			set.add(fileItem.getPreviousPartitionKey());
		}
		try {
			penaltyCandidateService.insertAll(new EntityRequest<>(set));
		} catch (ApiException e) {
			LOGGER.error(messageTranslator.getMessage("error.candidate.insert", e.getMessage()), e);
		}
	}

	private void handleFranchigiaPeriod(final List<PenaltyEntity> franchigiaPenalties) {
		try {
			franchigiaAggregator.determineAggregationForFranchigiaPenalties(franchigiaPenalties);
			penaltyDAO.updatePenalties(franchigiaPenalties);
		} catch (ApiException e) {
			LOGGER.error(messageTranslator.getMessage("error.determining.franchigia.aggregations"), e);
		}

	}

	private List<String> getCodeTts(final List<FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity>> fileItems) {
		return fileItems.stream().map(i -> i.getItem().getCodeTt()).collect(Collectors.toList());
	}

	private FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> process(
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final Map<String, List<PenaltyEntity>> penaltyMap, final List<PenaltyEntity> penalties,
			final CompletableFuture<List<ServiceTypeTranscodeJpubEntity>> regulatedJpubMap,
			final CompletableFuture<List<ServiceTypeTranscodeJpubEntity>> nonRegulatedJpubMap,
			final Map<String, RegistryResolutionBasic> resolutionMap,
			final Map<String, List<FranchigiaPeriodResponse>> franchigiaPeriodsMap,
			final Map<String, List<RetrivePeriodIdResponse>> penaltyPeriodsMap,
			final List<RequestJpubPrice> jpubRequests, final List<PenaltyResetSetupEntity> rules) {
		try {
			validate(fileItem);
			OperaAssuranceLoadPenaltyEntity operaEntity = fileItem.getItem();
			operaEntity.setAccountId(fileItem.getAccountId());
			operaEntity.setParentAccountId(fileItem.getParentAccountId());

			if (!fileItem.isValid()) {
				return fileItem;
			}

			PenaltyEntity penaltyEntity = getPenalty(penaltyMap.get(fileItem.getItem().getCodeTt()));
			LOGGER.debug("Inside processing opera penalties :  penalty entity in batch {}", penaltyEntity);

			FileLoadUtils.translate(fileItem.getItem(), fileItem.getDocumentCode());
			enrich(fileItem, resolutionMap);
			// List<PenaltyResetSetupEntity> rules =
			// penaltyResetSetupDAO.findAllActive("OPERA");

			if (fileItem.getItem().getSlaTypeDef() == null
					|| "Premium Gold".equalsIgnoreCase(fileItem.getItem().getSlaTypeDef())
					|| "Premium H24".equalsIgnoreCase(fileItem.getItem().getSlaTypeDef())) {
				fileItem.getItem().setFaultOpeningTime(null);
			}

			if (!fileItem.isValid()) {
				return fileItem;
			}

			if (fileItem.getItem().getChargeAmount() != null) {
				fileItem.setCheckAmount(false);
			} else if (penaltyEntity == null || (penaltyEntity != null && penaltyEntity.getChargeAmount() == null)) {
				fileItem.setCheckAmount(true);
			} else if (fileItem.getItem().getChargeAmount() == null) {
				fileItem.setCheckAmount(true);
			}

			if (fileItem.getItem().getChargeAmount() == null && penaltyEntity != null) {
				fileItem.getItem().setChargeAmount(penaltyEntity.getChargeAmount());
			}

			fileItem.setCancelledStatus(false);
			// if (fileItem.getItem().getOaoClosureTtDesc() != null && !"Telecom
			// Italia".equals(fileItem.getItem().getOaoClosureTtDesc())) {
			// updateCancelled(fileItem.getItem());
			// fileItem.setCheckAmount(false);
			// fileItem.setCancelledStatus(true);
			// }

			ValorizationRuleWithDetail rule = SmsRuleCalculationUtils.findRule(fileItem,
					LocalDateTime.parse(fileItem.getItem().getComplaintReceivalDate(),
							DateTimeFormatter.ofPattern(translationDateValidator
									.determineDateFormat(fileItem.getItem().getComplaintReceivalDate()))),
					fileItem.getItem().getResolutionIgnore());

			if (rule == null) {
				fileItem.setValid(false);
				fileItem.setErrorMessage(messageTranslator.getMessage("no.matching.valorization.rule"));
				setReconciliationKey(null, fileItem);
				return fileItem;
			}

			if (rule != null) {
				enrichValuesDependingOnRule(fileItem, rule);
			}

			// PEN-PROD-252
			BigDecimal chargeAmount = null;
			if (fileItem.isCheckAmount()) {
				chargeAmount = chargeAmountConfigurationDAO.findChargeAmountByFamily(fileItem.getItem().getFamily());
			}

			SmsRuleCalculationUtils.calculateRules(fileItem, rule, chargeAmount);

			if (rule != null && rule.getFranchigiaInd().equalsIgnoreCase("1")) {
				if (fileItem.getItem().getRepairOnThresholdS1() != null
						&& "S".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS1())) {
					fileItem.getItem().setTotalDelay1(BigDecimal.ZERO);
					fileItem.getItem().setPenaltyAmount1(BigDecimal.ZERO);
				} else if (fileItem.getItem().getRepairOnThresholdS1() == null
						|| fileItem.getItem().getRepairOnThresholdS1().isEmpty()) {
					fileItem.getItem().setTotalDelay1(BigDecimal.ZERO);
					fileItem.getItem().setPenaltyAmount1(BigDecimal.ZERO);
				}
				if (fileItem.getItem().getRepairOnThresholdS2() != null
						&& "S".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS2())) {
					fileItem.getItem().setTotalDelay2(BigDecimal.ZERO);
					fileItem.getItem().setPenaltyAmount2(BigDecimal.ZERO);
				} else if (fileItem.getItem().getRepairOnThresholdS2() == null
						|| fileItem.getItem().getRepairOnThresholdS2().isEmpty()) {
					fileItem.getItem().setTotalDelay2(BigDecimal.ZERO);
					fileItem.getItem().setPenaltyAmount2(BigDecimal.ZERO);
				}
				if (fileItem.getItem().getRepairOnThresholdS3() != null
						&& "S".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS3())) {
					fileItem.getItem().setTotalDelay3(BigDecimal.ZERO);
					fileItem.getItem().setPenaltyAmount3(BigDecimal.ZERO);
				} else if (fileItem.getItem().getRepairOnThresholdS3() == null
						|| fileItem.getItem().getRepairOnThresholdS3().isEmpty()) {
					fileItem.getItem().setTotalDelay3(BigDecimal.ZERO);
					fileItem.getItem().setPenaltyAmount3(BigDecimal.ZERO);
				}

			}

			if ((fileItem.isAmountInd()
					&& ((penaltyEntity != null && penaltyEntity.getChargeAmount() == null) || penaltyEntity == null))
					&& fileItem.getItem() != null && fileItem.getItem().getOaoClosureTtDesc() != null
					&& "Telecom Italia".equals(fileItem.getItem().getOaoClosureTtDesc())
					&& (StringUtils.isNotEmpty(fileItem.getItem().getReportingType())
							&& !fileItem.getItem().getReportingType().equalsIgnoreCase("supporto"))
					&& ((StringUtils.isNotEmpty(fileItem.getItem().getUnbundled())
							&& !fileItem.getItem().getUnbundled().equalsIgnoreCase("s"))
							|| (StringUtils.isNotEmpty(fileItem.getItem().getAdditionalServices())
									&& !fileItem.getItem().getAdditionalServices().equalsIgnoreCase("s")))) {
				RequestJpubPrice request = new RequestJpubPrice();
				createOperaRequestJpubPrice(fileItem.getItem(), request, regulatedJpubMap.get(),
						nonRegulatedJpubMap.get());
				jpubRequests.add(request);
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.WAITING_FOR_JPUB_RESPONSE.value());
				setReconciliationKey(null, fileItem);
			} else {
				log.info("process else from OperaPeanltyFileItemProcessorV2 -> handlePenalty {}, {}", penaltyEntity,
						fileItem);
				ArrayList<PenaltyEntity> penaltyRecords = handlePenalty(penaltyEntity, fileItem);
				for (PenaltyEntity penaltyRecord : penaltyRecords) {

					penaltyRecord.setOaoClosureTtDesc(fileItem.getItem().getOaoClosureTtDesc());
					Long franchigiaPeriodIdOld = penaltyRecord.getFranchigiaPeriodId();
					Long aggregationId = penaltyRecord.getAggregationId();
					String closedPeriod = penaltyRecord.getFranchigiaPeriodClosedFlag();

					if (penaltyRecord != null && setFranchigaPeriod(fileItem, franchigiaPeriodsMap, penaltyRecord)
							&& setPenaltyPeriod(fileItem, penaltyPeriodsMap, penaltyRecord)) {

						Long franchigiaPeriodIdNew = penaltyRecord.getFranchigiaPeriodId();
						if (franchigiaPeriodIdNew != null && franchigiaPeriodIdOld != null
								&& !franchigiaPeriodIdOld.equals(franchigiaPeriodIdNew)
								&& "1".equalsIgnoreCase(closedPeriod)) {
							franchigiaAggregator.updateAggregation(aggregationId);
						}
						if (franchigiaPeriodIdNew != null && franchigiaPeriodIdOld != null
								&& !franchigiaPeriodIdOld.equals(franchigiaPeriodIdNew)
								&& "0".equalsIgnoreCase(penaltyRecord.getFranchigiaPeriodClosedFlag())) {
							penaltyRecord.setAggregationId(null);
						}
						penaltyRecord.setParentAccountId(fileItem.getParentAccountId());
						if (fileItem.getParentAccountId() == null) {

							penaltyRecord.setHierarchyId(fileItem.getAccountShortCode());

							fileItem.getItem().setHierarchyId(penaltyRecord.getHierarchyId());

						} else if (franchigiaPeriodIdNew != null
								&& franchigiaPeriodsMap.get(penaltyRecord.getFranchigiaFrequency()) != null) {

							LocalDateTime franchisePeriodEndDate = franchigiaPeriodsMap
									.get(penaltyRecord.getFranchigiaFrequency()).stream()
									.filter(f -> f.getId().equals(penaltyRecord.getFranchigiaPeriodId())).findFirst()
									.orElse(new FranchigiaPeriodResponse()).getEndDate();

							penaltyRecord.setHierarchyId(getHierarchyId(franchisePeriodEndDate,
									fileItem.getParentAccountId(), fileItem.getValorizationRegistrationDate(),
									fileItem.getValorizationRuleDate(), penaltyRecord.getReferentDate(),
									fileItem.getParentAccountShortCode(), fileItem.getAccountShortCode()));

							fileItem.getItem().setHierarchyId(penaltyRecord.getHierarchyId());
						} else {
							if (fileItem.getValorizationRuleDate() != null && penaltyRecord.getReferentDate() != null
									&& fileItem.getValorizationRuleDate().isBefore(penaltyRecord.getReferentDate())) {
								penaltyRecord.setHierarchyId(fileItem.getParentAccountShortCode());
								penaltyRecord.setParentAccountId(fileItem.getParentAccountId());
								fileItem.getItem().setHierarchyId(penaltyRecord.getHierarchyId());

							} else {
								penaltyRecord.setHierarchyId(fileItem.getAccountShortCode());
								penaltyRecord.setParentAccountId(fileItem.getParentAccountId());
								fileItem.getItem().setHierarchyId(penaltyRecord.getHierarchyId());
							}

						}

						// PEN-PROD-172
						if (fileItem.getItem().getManualUpdateInd() != null) {
							penaltyRecord.setManualLoadUser(fileItem.getItem().getModifiedBy());
							log.debug("setManualLoadUser {} for penaltyRecord {} ", fileItem.getItem().getModifiedBy(),
									penaltyRecord.getId());
						}

						// PEN-PROD-226 - update pntPenaltyAggregation // FIX K01
						log.debug("================================================");
						log.debug("penaltyRecord FranchigiaFlag before reset[{}]", penaltyRecord.getFranchigiaFlag());
						if (penaltyRecord.getFranchigiaFlag() != null
								&& "1".equalsIgnoreCase(penaltyRecord.getFranchigiaFlag())) {
							log.debug("Franchigia Flag in penaltyRecord [{}] then call UpdateReceivedFlag=U",
									penaltyRecord.getFranchigiaFlag());
							franchigiaAggregator.updateAggregation(aggregationId);
						}

						// PEN-PROD-204C
						String tipoPenale = fileItem.getItem().getTipoPenale();
						log.debug("TIPO_PENALE value is '{}'", tipoPenale);

						// Validate tipoPenale before processing
						if (!"1".equals(tipoPenale) && !"2".equals(tipoPenale)
								&& (tipoPenale != null && !tipoPenale.trim().isEmpty())) {
							fileItem.setValid(false);
							fileItem.setErrorCode("ERR_TIPO_PENALE");
							fileItem.setErrorMessage("Valore TIPO_PENALE non previsto");
							log.error("Unexpected value for TIPO_PENALE: [{}]. Expected '1', '2' or empty.",
									tipoPenale);
							return fileItem;
						}

						// Store original penaleOneStep value and set to "N" if null or empty
						String penaleOneStep = fileItem.getItem().getPenaleOneStep();
						boolean wasPenaleOneStepNull = (penaleOneStep == null);
						boolean wasPenaleOneStepEmpty = (penaleOneStep != null && penaleOneStep.trim().isEmpty());
						boolean wasPenaleOneStepNullOrEmpty = wasPenaleOneStepNull || wasPenaleOneStepEmpty;
						log.debug("Original penaleOneStep value: [{}], isNull: {}, isEmpty: {}", penaleOneStep,
								wasPenaleOneStepNull, wasPenaleOneStepEmpty);

						if ("2".equals(tipoPenale) && wasPenaleOneStepNullOrEmpty) {
							log.debug("PenaleOneStep is null or empty, setting to 'N' for processing.");
							fileItem.getItem().setPenaleOneStep("N");
							penaleOneStep = "N";
						}
						// PEN-PROD-204C

						// Determine if the penalty will be parked based on the given conditions.
						boolean willBeParked = ("F".equals(penaltyRecord.getManualUpdateInd())
								|| "C".equals(penaltyRecord.getManualUpdateInd())
								|| PenaltyStatus.IN_APPROVAL.value().equalsIgnoreCase(penaltyRecord.getStatus()))
								&& !fileItem.isCancelledStatus();

						log.info("Opera will be parked: {}", willBeParked);

						if (willBeParked && penaltyEntity != null) {
							log.info("Skipping applyOPERAResetRules as the penalty will be parked.");
						} else {
							log.info("Applying OPERA reset rules.");
							SmsRuleCalculationUtils.applyOPERAResetRules(fileItem.getItem(), penaltyRecord, rules);
						}

						// PEN-PROD-204C
						if ("2".equals(tipoPenale)) {
							// Check if this is an Assurance penalty (penalty type code "13" or "14")
							boolean isAssurance = "13".equals(fileItem.getItem().getSourcePenaltyTypeCode())
									|| "14".equals(fileItem.getItem().getSourcePenaltyTypeCode());

							String resetIndicator = penaltyRecord.getResetIndicator();

							log.debug("isAssurance: {}, resetIndicator: {}, penaleOneStep: {}", isAssurance,
									resetIndicator, penaleOneStep);

							if (isAssurance) {
								if ("1".equals(resetIndicator)) {
									log.debug(
											"Assurance penalty with resetIndicator=1: Restoring original penaleOneStep value.");
									// Restore original value (null or empty)
									if (wasPenaleOneStepNullOrEmpty && "N".equalsIgnoreCase(penaleOneStep.trim())) {
										if (wasPenaleOneStepNull) {
											fileItem.getItem().setPenaleOneStep(null);
											log.debug("PenaleOneStep restored to original null value.");
										} else {
											fileItem.getItem().setPenaleOneStep("");
											log.debug("PenaleOneStep restored to original empty string value.");
										}
									}
								} else {
									// resetIndicator is "0" in Assurance
									log.debug(
											"Assurance penalty with resetIndicator=0: Validating penaleOneStep value.");
									if ("S".equalsIgnoreCase(penaleOneStep.trim())) {
										// Scenario 4: Do nothing
										log.debug(
												"Scenario 4: Fibercop penalty set (penaleOneStep='S'), no action taken.");
									} else {
										// Unexpected value, set record as invalid
										fileItem.setValid(false);
										fileItem.setErrorCode("ERR_PENALE_ONE_STEP");
										fileItem.setErrorMessage("Valore PENALE_ONE_STEP non previsto");
										log.error(
												"Unexpected value for PENALE_ONE_STEP: [{}]. Expected 'S' for Assurance penalty with resetIndicator=0.",
												penaleOneStep);
										return fileItem;
									}
								}
							}
						}
						// PEN-PROD-204C

						log.debug("penaltyRecord FranchigiaFlag after reset[{}]", penaltyRecord.getFranchigiaFlag());
						penalties.add(penaltyRecord);

					}
				}
			}

		} catch (Exception e) {
			LOGGER.error(messageTranslator.getMessage("opera.item.processing.error"), e);
			fileItem.setValid(false);
			fileItem.setErrorCode("ERR99");
			fileItem.setErrorMessage(e.getMessage());
			setReconciliationKey(null, fileItem);
		}
		return fileItem;

	}

	private String getHierarchyId(final LocalDateTime periodEndDate, final String parentAccountId,

			final LocalDateTime dataRegistractionMrg, final LocalDateTime dataCutoffMrg,
			final LocalDateTime referentDate, final String parentAccountShortCode, final String accountShortCode) {
		if (periodEndDate == null) {
			return accountShortCode;
		}

		if (parentAccountId == null || (dataRegistractionMrg != null && dataRegistractionMrg.isAfter(periodEndDate))
				|| (dataRegistractionMrg != null && !dataRegistractionMrg.isAfter(periodEndDate))
						&& (dataCutoffMrg != null && referentDate != null && referentDate.isBefore(dataCutoffMrg))) {
			return accountShortCode;
		} else if ((dataRegistractionMrg != null && referentDate != null
				&& !dataRegistractionMrg.isAfter(periodEndDate)) && (!referentDate.isBefore(dataCutoffMrg))) {
			return parentAccountShortCode;
		}
		return accountShortCode;

	}

	private boolean setPenaltyPeriod(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final Map<String, List<RetrivePeriodIdResponse>> penaltyPeriodsMap, final PenaltyEntity penalty) {
		LocalDateTime referentDate = penalty.getClosureDateTt();
		penalty.setReferentDate(referentDate);
		if (penalty.getPenaltyFrequency() == null) {
			setError(fileItem, "ERR##", messageTranslator.getMessage("cannot.find.penalty.period",
					penalty.getPenaltyFrequency(), referentDate));
			return false;
		}
		List<RetrivePeriodIdResponse> periods = penaltyPeriodsMap.get(penalty.getPenaltyFrequency()) != null
				? penaltyPeriodsMap.get(penalty.getPenaltyFrequency())
				: new ArrayList<>();
		for (RetrivePeriodIdResponse period : periods) {
			if (penalty.getPenaltyFrequency() != null && penalty.getPenaltyFrequency().equals(period.getFrequency())
					&& DateUtil.isBetween(referentDate, period.getStartDate(), period.getEndDate())) {
				penalty.setPenaltyPeriodId(period.getPeriodId());
				penalty.setPenaltyPeriodName(period.getPeriodName());
				return true;
			}
		}
		if (penalty.getPenaltyPeriodId() == null) {
			setError(fileItem, "ERR##", messageTranslator.getMessage("cannot.find.penalty.period",
					penalty.getPenaltyFrequency(), referentDate));
		}
		return false;
	}

	private boolean setFranchigaPeriod(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final Map<String, List<FranchigiaPeriodResponse>> franchigiaPeriodsMap, final PenaltyEntity penalty) {
		if (penalty.getFranchigiaFlag() == null || "0".equalsIgnoreCase(penalty.getFranchigiaFlag())) {
			return true;
		}
		LocalDateTime referentDate = penalty.getClosureDateTt();
		if (penalty.getFranchigiaFrequency() == null) {
			setError(fileItem, "ERR##", messageTranslator.getMessage("cannot.find.franchiga.period",
					penalty.getFranchigiaFrequency(), referentDate));
			return false;
		}
		List<FranchigiaPeriodResponse> periods = franchigiaPeriodsMap.get(penalty.getFranchigiaFrequency()) != null
				? franchigiaPeriodsMap.get(penalty.getFranchigiaFrequency())
				: new ArrayList<>();
		for (FranchigiaPeriodResponse period : periods) {
			if (penalty.getFranchigiaFrequency() != null
					&& penalty.getFranchigiaFrequency().equals(period.getFrequency())
					&& DateUtil.isBetween(referentDate, period.getStartDate(), period.getEndDate())) {
				penalty.setFranchigiaPeriodId(period.getId());
				if ("1".equalsIgnoreCase(penalty.getFranchigiaFlag()) && penalty.getFranchigiaPeriodId() != null
						&& "Closed".equalsIgnoreCase(period.getStatus())) {
					penalty.setFranchigiaPeriodClosedFlag("1");
				} else {
					penalty.setFranchigiaPeriodClosedFlag("0");
				}
				return true;
			}
		}
		if (penalty.getFranchigiaPeriodId() == null) {
			setError(fileItem, "ERR##", messageTranslator.getMessage("cannot.find.franchiga.period",
					penalty.getFranchigiaFrequency(), referentDate));
		}
		return false;
	}

	private PenaltyEntity getPenalty(final List<PenaltyEntity> list) {
		if (list == null || list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			for (PenaltyEntity p : list) {
				if (!"Approved".equalsIgnoreCase(p.getStatus())) {
					return p;
				}
			}
			return list.get(0);
		} else {
			return list.get(0);
		}
	}

	private void enrichValuesDependingOnRule(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final ValorizationRuleWithDetail rule) {
		OperaAssuranceLoadPenaltyEntity operaLoadEntity = fileItem.getItem();

		for (ValorizationRuleDetailBasic valRule : rule.getDetails()) {
			if (valRule.getSeqno().equals(new Short("1"))) {
				fileItem.setSlaThresholdValue1(valRule.getSla());
			} else if (valRule.getSeqno().equals(new Short("2"))) {
				fileItem.setSlaThresholdValue2(valRule.getSla());
			} else if (valRule.getSeqno().equals(new Short("3"))) {
				fileItem.setSlaThresholdValue3(valRule.getSla());
			}
		}
		if (fileItem.getFranchigiaInd() == null) {
			fileItem.setFranchigiaInd(rule.getFranchigiaInd());
		}
		if (fileItem.getValorizationRuleMeasureUnit() == null) {
			fileItem.setValorizationRuleMeasureUnit(rule.getMeasureUnit());
		}
		if (fileItem.getFranchigiaFrequency() == null) {
			fileItem.setFranchigiaFrequency(rule.getFranchigiaFrequency());
		}
		if (fileItem.getPenaltyFrequency() == null) {
			fileItem.setPenaltyFrequency(rule.getPenaltyFrequency());
		}

		if (!rule.getFranchigiaInd().equalsIgnoreCase("0")) {

			if ("N".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS1())
					|| "N".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS2())) {
				fileItem.setCalculationNeeded(true);
			}

			if (MeasureUnit.DELAY_CALENDAR_HOURS.value().equalsIgnoreCase(rule.getMeasureUnit())
					|| MeasureUnit.DELAY_WORKING_HOURS.value().equalsIgnoreCase(rule.getMeasureUnit())) {
				if (operaLoadEntity.getDelayCalendarSecondsS1() != null
						&& !operaLoadEntity.getDelayCalendarSecondsS1().isEmpty()) {
					operaLoadEntity.setTotalDelay1(new BigDecimal(operaLoadEntity.getDelayCalendarSecondsS1())
							.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
				}
				if (operaLoadEntity.getDelayCalendarSecondsS2() != null
						&& !operaLoadEntity.getDelayCalendarSecondsS2().isEmpty()) {
					operaLoadEntity.setTotalDelay2(new BigDecimal(operaLoadEntity.getDelayCalendarSecondsS2())
							.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
				}
				if (operaLoadEntity.getDelayCalendarSecondsS3() != null
						&& !operaLoadEntity.getDelayCalendarSecondsS3().isEmpty()) {
					operaLoadEntity.setTotalDelay3(new BigDecimal(operaLoadEntity.getDelayCalendarSecondsS3())
							.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
				}
			} else if (MeasureUnit.DELAY_WORKING_DAYS.value().equalsIgnoreCase(rule.getMeasureUnit())) {
				if (operaLoadEntity.getDelayWorkingDaysS1() != null
						&& !operaLoadEntity.getDelayWorkingDaysS1().isEmpty()) {
					operaLoadEntity.setTotalDelay1(new BigDecimal(operaLoadEntity.getDelayWorkingDaysS1()));
				} else {
					operaLoadEntity.setTotalDelay1(new BigDecimal(0));
				}

				if (operaLoadEntity.getDelayWorkingDaysS2() != null
						&& !operaLoadEntity.getDelayWorkingDaysS2().isEmpty()) {
					operaLoadEntity.setTotalDelay2(new BigDecimal(operaLoadEntity.getDelayWorkingDaysS2()));
				} else {
					operaLoadEntity.setTotalDelay2(new BigDecimal(0));
				}
				if (operaLoadEntity.getDelayWorkingDaysS3() != null
						&& !operaLoadEntity.getDelayWorkingDaysS3().isEmpty()) {
					operaLoadEntity.setTotalDelay3(new BigDecimal(operaLoadEntity.getDelayWorkingDaysS3()));
				} else {
					operaLoadEntity.setTotalDelay3(new BigDecimal(0));
				}
			}
			operaLoadEntity.setTotalDelay(new BigDecimal(0));
			operaLoadEntity.setPenaltyAmount(new BigDecimal(0));

		} else {
			if (MeasureUnit.DELAY_CALENDAR_HOURS.value().equalsIgnoreCase(rule.getMeasureUnit())
					|| MeasureUnit.DELAY_WORKING_HOURS.value().equalsIgnoreCase(rule.getMeasureUnit())) {
				operaLoadEntity.setTotalDelay(new BigDecimal(operaLoadEntity.getDelayCalendarSecondsS1())
						.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
			} else if (MeasureUnit.DELAY_WORKING_DAYS.value().equalsIgnoreCase(rule.getMeasureUnit())) {
				operaLoadEntity.setTotalDelay(new BigDecimal(operaLoadEntity.getDelayWorkingDaysS1()));
			}
		}

		if (MeasureUnit.DELAY_CALENDAR_HOURS.value().equalsIgnoreCase(rule.getMeasureUnit())
				|| MeasureUnit.DELAY_WORKING_HOURS.value().equalsIgnoreCase(rule.getMeasureUnit())) {
			if (operaLoadEntity.getDelayCalendarSecondsS1() != null) {
				fileItem.setTotalHoursDelay(new BigDecimal(operaLoadEntity.getDelayCalendarSecondsS1())
						.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
			}
		}
	}

	private ArrayList<PenaltyEntity> handlePenalty(final PenaltyEntity penaltyEntity,
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) throws ParseException {
		ArrayList<PenaltyEntity> penalties = new ArrayList<>();
		if (penaltyEntity == null) {
			log.info("handlePenalty penaltyEntity = null");
			penalties.add(createPenalty(fileItem));
			if (!fileItem.isCancelledStatus()) {
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
			}
			return penalties;
		} else {
			if ("E".equalsIgnoreCase(fileItem.getItem().getReprocessInd())) {
				penalties.add(reprocessExtended(penaltyEntity, fileItem));
				return penalties;
			} else if ("F".equalsIgnoreCase(fileItem.getItem().getReprocessInd())) {
				log.info("handlePenalty reprocessInd = F -> reprocessFull {}, {}", penaltyEntity, fileItem);
				penalties.add(reprocessFull(penaltyEntity, fileItem));
				return penalties;
			} else if ("F".equals(penaltyEntity.getManualUpdateInd()) || "C".equals(penaltyEntity.getManualUpdateInd())
					|| PenaltyStatus.IN_APPROVAL.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				log.info("handlePenalty reprocessInd = F/C/In Approval -> reprocessFull {}, {}", penaltyEntity,
						fileItem);
				operaAssuranceLoadPenaltyDAO.updateParkedOldRecord(fileItem.getItem().getCodeTt());
				if (!fileItem.isCancelledStatus()) {
					log.info(
							"handlePenalty reprocessInd = F/C/In Approval + status not cancelled -> reprocessFull {}, {}",
							penaltyEntity, fileItem);
					updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PARKED.value());
				}
				penalties.add(penaltyEntity);
				return penalties;
			} else if (PenaltyStatus.ACTIVE.value().equalsIgnoreCase(penaltyEntity.getStatus())
					|| PenaltyStatus.IN_RECONCIlIATION.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				updateExistingPenalty(fileItem, penaltyEntity);
				if (!fileItem.isCancelledStatus()) {
					updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
				}
				penalties.add(penaltyEntity);
				return penalties;
			} else if (PenaltyStatus.APPROVED.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				penalties.add(handlingApprovedRecords(penaltyEntity, fileItem));
				return penalties;
			} else {
				penalties.add(penaltyEntity);
				return penalties;
			}
		}

	}

	private PenaltyEntity handlingApprovedRecords(final PenaltyEntity penaltyEntity,
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) throws ParseException {
		PenaltyEntity newPenaltyEntity = preparePenalty(fileItem);
		if (newPenaltyEntity.equals(penaltyEntity)) {
			fileItem.getItem().setErrorDescription("Record Clone di uno già benestariato commercialmente");
			updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.CLONED.value());
			return penaltyEntity;
		} else {
			fileItem.setPrevAcceptedAmount(penaltyEntity.getAcceptedAmount());
			PenaltyEntity createdPenalty = createPenalty(fileItem);
			if (!fileItem.isCancelledStatus()) {
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
			}
			return createdPenalty;
		}
	}

	private PenaltyEntity reprocessFull(final PenaltyEntity penaltyEntity,
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) throws ParseException {
		log.info("reprocessFull {}, {}", penaltyEntity, fileItem);
		if (("13".equals(penaltyEntity.getPenaltyCode()) || "14".equals(penaltyEntity.getPenaltyCode()))
				&& !"Telecom Italia".equals(penaltyEntity.getOaoClosureTtDesc())) {
			// PEN-PROD-226 - update pntPenaltyAggregation
			if (penaltyEntity.getFranchigiaFlag() != null && "1".equalsIgnoreCase(penaltyEntity.getFranchigiaFlag())) {
				log.debug("Franchigia Flag before reset [{}]", penaltyEntity.getFranchigiaFlag());
				if (penaltyEntity.getAggregationId() != null) {
					Long aggregationId = penaltyEntity.getAggregationId();
					franchigiaPeriodDAO.updateAggregationsUsingAggregationId(aggregationId);
					log.debug("Updated aggregationId [{}]", aggregationId);
				}
			}
			log.debug("reprocessFull - Reset Franchigia Flag=0");
			penaltyEntity.setFranchigiaFlag("0");
			penaltyEntity.setAggregationId(null);
			penaltyEntity.setSlaThresholdValue(new BigDecimal(100));
			penaltyEntity.setPenaltyAmount(new BigDecimal(0));
			// PEN-PROD-226-B02 added reset
			penaltyEntity.setInFranchigia1(null);
			penaltyEntity.setInFranchigia2(null);
			penaltyEntity.setInFranchigia3(null);
			penaltyEntity.setFranchigiaPeriodId(null);
			penaltyEntity.setFranchigiaPeriodClosedFlag(null);
			penaltyEntity.setFranchigiaFrequency(null);
		}
		if ("F".equals(penaltyEntity.getManualUpdateInd()) || "C".equals(penaltyEntity.getManualUpdateInd())) {
			if (PenaltyStatus.IN_APPROVAL.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				operaAssuranceLoadPenaltyDAO.updateParkedOldRecord(fileItem.getItem().getCodeTt());
				log.info("reprocessFull -> with manual update ind F/C in approval status going to Parked for {} ",
						fileItem.getItem());
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PARKED.value());
				return penaltyEntity;
			} else if (PenaltyStatus.APPROVED.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				return handlingApprovedRecords(penaltyEntity, fileItem);
			} else if (PenaltyStatus.ACTIVE.value().equalsIgnoreCase(penaltyEntity.getStatus())
					|| PenaltyStatus.IN_RECONCIlIATION.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				updateExistingPenalty(fileItem, penaltyEntity);
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
				return penaltyEntity;
			} else {
				return penaltyEntity;
			}
		} else {
			if (PenaltyStatus.IN_APPROVAL.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				operaAssuranceLoadPenaltyDAO.updateParkedOldRecord(fileItem.getItem().getCodeTt());
				log.info("reprocessFull -> from else in approval status going to Parked for {} ", fileItem.getItem());
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PARKED.value());
				return penaltyEntity;
			} else if (PenaltyStatus.APPROVED.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				return handlingApprovedRecords(penaltyEntity, fileItem);
			} else if (PenaltyStatus.ACTIVE.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				updateExistingPenalty(fileItem, penaltyEntity);
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
				return penaltyEntity;
			} else {
				return penaltyEntity;
			}
		}
	}

	private PenaltyEntity reprocessExtended(final PenaltyEntity penaltyEntity,
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) throws ParseException {
		if (("13".equals(penaltyEntity.getPenaltyCode()) || "14".equals(penaltyEntity.getPenaltyCode()))
				&& !"Telecom Italia".equals(penaltyEntity.getOaoClosureTtDesc())) {
			// PEN-PROD-226 - update pntPenaltyAggregation
			if (penaltyEntity.getFranchigiaFlag() != null && "1".equalsIgnoreCase(penaltyEntity.getFranchigiaFlag())) {
				log.debug("Franchigia Flag before reset [{}]", penaltyEntity.getFranchigiaFlag());
				if (penaltyEntity.getAggregationId() != null) {
					Long aggregationId = penaltyEntity.getAggregationId();
					franchigiaPeriodDAO.updateAggregationsUsingAggregationId(aggregationId);
					log.debug("Updated aggregationId [{}]", aggregationId);
				}
			}
			log.debug("reprocessExtended - Reset Franchigia Flag=0");
			penaltyEntity.setFranchigiaFlag("0");
			penaltyEntity.setAggregationId(null);
			penaltyEntity.setSlaThresholdValue(new BigDecimal(100));
			penaltyEntity.setPenaltyAmount(new BigDecimal(0));
			// PEN-PROD-226-B02 added reset
			penaltyEntity.setInFranchigia1(null);
			penaltyEntity.setInFranchigia2(null);
			penaltyEntity.setInFranchigia3(null);
			penaltyEntity.setFranchigiaPeriodId(null);
			penaltyEntity.setFranchigiaPeriodClosedFlag(null);
			penaltyEntity.setFranchigiaFrequency(null);
		}
		if ("F".equals(penaltyEntity.getManualUpdateInd()) || "C".equals(penaltyEntity.getManualUpdateInd())) {
			if (PenaltyStatus.IN_APPROVAL.value().equalsIgnoreCase(penaltyEntity.getStatus())
					|| PenaltyStatus.IN_RECONCIlIATION.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				operaAssuranceLoadPenaltyDAO.updateParkedOldRecord(fileItem.getItem().getCodeTt());
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PARKED.value());
				return penaltyEntity;
			} else if (PenaltyStatus.APPROVED.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				return handlingApprovedRecords(penaltyEntity, fileItem);
			} else if (PenaltyStatus.ACTIVE.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				updateExistingPenalty(fileItem, penaltyEntity);
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
				return penaltyEntity;
			} else {
				return penaltyEntity;
			}
		} else {
			if (PenaltyStatus.IN_APPROVAL.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				operaAssuranceLoadPenaltyDAO.updateParkedOldRecord(fileItem.getItem().getCodeTt());
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PARKED.value());
				return penaltyEntity;
			} else if (PenaltyStatus.APPROVED.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				return handlingApprovedRecords(penaltyEntity, fileItem);
			} else if (PenaltyStatus.ACTIVE.value().equalsIgnoreCase(penaltyEntity.getStatus())) {
				updateExistingPenalty(fileItem, penaltyEntity);
				updateItem(fileItem.getItem(), FileLoadItemsPenaltyStatus.PROCESSED.value());
				return penaltyEntity;
			} else {
				return penaltyEntity;
			}
		}
	}

	private void updateExistingPenalty(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final PenaltyEntity entity) {

		setPenaltyData(entity, fileItem);
		if (!"1".equalsIgnoreCase(entity.getFranchigiaFlag())) {
			if (fileItem.getItem() != null && fileItem.getItem().getPenaltyAmount() != null) {
				entity.setPenaltyAmount(fileItem.getItem().getPenaltyAmount().setScale(2, RoundingMode.HALF_EVEN));
			} else {
				log.warn(
						"OperaAssuranceLoadPenaltyEntity updateExistingPenalty Penalty Amount is null for entity {}, fileItem {}",
						entity, fileItem);
				entity.setPenaltyAmount(BigDecimal.ZERO);
			}
		}
		entity.setValorizationRuleId(fileItem.getValorizationRuleId());
		entity.setCalculationRuleId(fileItem.getCalculationRuleId());
		entity.setModified(LocalDateTime.now());
		// entity.setModifiedBy("system");
		entity.setUpdatedInd(Indicator.TRUE.value());
	}

	private void updateItem(final OperaAssuranceLoadPenaltyEntity item, final String status) {
		item.setStatus(status);
		item.setModified(LocalDateTime.now());
		// item.setUpdateReceived("1");
		// item.setModifiedBy("system");
	}

	private PenaltyEntity createPenalty(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem)
			throws ParseException {
		PenaltyEntity entity = preparePenalty(fileItem);
		return entity;
	}

	private PenaltyEntity preparePenalty(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) {
		PenaltyEntity entity = new PenaltyEntity();

		BeanUtils.copyProperties(fileItem.getItem(), entity, "chargeAmount");
		entity.setId(null);
		entity.setCreated(LocalDateTime.now());
		entity.setStatus(PenaltyStatus.ACTIVE.value());
		entity.setModified(null);
		entity.setModifiedBy(null);
		setPenaltyData(entity, fileItem);
		return entity;
	}

	private FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> enrich(
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final Map<String, RegistryResolutionBasic> resolutionMap) {
		if (StringUtils.isNotEmpty(fileItem.getItem().getDelayWorkingDaysS1())) {
			fileItem.getItem().setTotalDelay(new BigDecimal(fileItem.getItem().getDelayWorkingDaysS1()));
		}

		if (fileItem.getItem().getSourceResolutionCode() != null) {
			RegistryResolutionBasic resolution = resolutionMap.get(fileItem.getItem().getSourceResolutionCode());
			if (resolution == null) {
				fileItem.setValid(false);
				fileItem.setErrorCode("ERR04");
				fileItem.setErrorMessage(messageTranslator.getMessage("no.corresponding.resolution"));
				setReconciliationKey(null, fileItem);
				LOGGER.error(messageTranslator.getMessage("processing.error",
						messageTranslator.getMessage("no.corresponding.resolution")));
			} else {
				fileItem.getItem().setSourceResolutionDescription(resolution.getResolutionName());
				fileItem.getItem().setResolutionCode(resolution.getCode());
			}
		}

		if (fileItem.getItem().getSlaTypeS2() != null
				&& fileItem.getItem().getSlaTypeS1().equalsIgnoreCase(fileItem.getItem().getSlaTypeS2())) {
			fileItem.getItem().setFaultOpeningTime("Festivi, Pre-festivi e Infrasettimanale fuori Orario Base");
		} else if (fileItem.getItem().getSlaTypeS2().isEmpty() || fileItem.getItem().getSlaTypeS2() == null) {
			fileItem.getItem().setFaultOpeningTime("Festivi, Pre-festivi e Infrasettimanale fuori Orario Base");
		} else {
			fileItem.getItem().setFaultOpeningTime("Infrasettimanale Orario Base (no Festivi)");
		}

		return fileItem;
	}

	private void setPenaltyData(final PenaltyEntity entity,
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) {
		entity.setPenaltyCode(fileItem.getItem().getSourcePenaltyTypeCode());
		entity.setResolutionCode(fileItem.getItem().getResolutionCode());
		entity.setResolutionDescription(fileItem.getItem().getSourceResolutionDescription());
		entity.setPenaltyDesc(fileItem.getItem().getSourcePenaltyTypeDesc());
		entity.setPenaltyTypology(penaltyDAO.getPenaltyTypology(fileItem.getItem().getSourcePenaltyTypeCode()));
		entity.setAccountfId("TIM");
		entity.setAccountId(fileItem.getAccountId());
		entity.setAccountName(fileItem.getAccountName());
		entity.setParentAccountId(fileItem.getParentAccountId());
		entity.setOperaServiceType(fileItem.getItem().getServiceType());

		if (fileItem.getItem().getChargeAmount() != null) {
			entity.setChargeAmount(fileItem.getItem().getChargeAmount());
		}

		entity.setLoadDate(LocalDateTime.now());
		if (fileItem.getItem().getTotalDelay() != null && !"1".equalsIgnoreCase(fileItem.getFranchigiaInd())) {
			entity.setTotalDelay(fileItem.getItem().getTotalDelay());
		} else if ("1".equalsIgnoreCase(fileItem.getFranchigiaInd())) {
			if (entity.getTotalDelay() == null) {
				entity.setTotalDelay(BigDecimal.ZERO);
			}

		}

		entity.setCodeTt(fileItem.getItem().getCodeTt());
		// entity.setSlaDate(LocalDateTime.now());
		// if (fileItem.getItem().getClosureDateTt() != null) {
		// entity.setOrderCompletionDate(LocalDateTime.parse(fileItem.getItem().getClosureDateTt(),
		// DateTimeFormatter.ofPattern(translationDateValidator.determineDateFormat(fileItem.getItem().getClosureDateTt()))));
		// }
		//
		// entity.setInPenalty("NO");
		entity.setManualUpdateInd(fileItem.getItem().getManualUpdateInd());

		entity.setPenaltyKeyCalculated(generatePenaltyCode(fileItem.getItem(), fileItem.getItem().getCodeTt()));
		// entity.setPenaltyDate(LocalDateTime.now().minusDays(2));
		entity.setSuspensionTime1(new BigDecimal(fileItem.getItem().getSuspensionTime()));
		entity.setSuspensionTime2(new BigDecimal(fileItem.getItem().getSuspensionTime2()));

		Map<String, ValidationRule> rulesMap = CacheManager.documentFormatCache.get(fileItem.getDocumentCode()).stream()
				.collect(Collectors.toMap(ValidationRule::getJavaFieldCode, Function.identity()));

		if (fileItem.getItem().getComplaintReceivalDate() != null) {
			String datePattern = "";
			if (rulesMap.get("complaintReceivalDate") != null) {
				datePattern = rulesMap.get("complaintReceivalDate").getFieldFormat();
			} else {
				datePattern = translationDateValidator
						.determineDateFormat(fileItem.getItem().getComplaintReceivalDate());
			}
			entity.setEventReceivalDate(LocalDateTime.parse(fileItem.getItem().getComplaintReceivalDate(),
					DateTimeFormatter.ofPattern(datePattern)));
		}

		entity.setResourceId(fileItem.getItem().getResourceIdTd());
		if (entity.getResourceId() == null) {
			entity.setResourceId(fileItem.getItem().getCodeTt());
		}

		entity.setScope(fileItem.getItem().getScope());
		entity.setReportingType(fileItem.getItem().getReportingType());
		entity.setOaoTicketType(fileItem.getItem().getOaoTicketType());
		entity.setServiceProblemDesc(fileItem.getItem().getServiceProblemDesc());
		entity.setTechnicalClassification(fileItem.getItem().getTechnicalClassification());

		if (fileItem.getItem().getExpiryDateS1() != null) {
			String datePattern = "";
			if (rulesMap.get("expiryDateS1") != null) {
				datePattern = rulesMap.get("expiryDateS1").getFieldFormat();
			} else {
				datePattern = translationDateValidator.determineDateFormat(fileItem.getItem().getExpiryDateS1());
			}
			entity.setExpiryDateS1(LocalDateTime.parse(fileItem.getItem().getExpiryDateS1(),
					DateTimeFormatter.ofPattern(datePattern)));
		}
		if (fileItem.getItem().getExpiryDateS2() != null && !fileItem.getItem().getExpiryDateS2().isEmpty()) {
			String datePattern = "";
			if (rulesMap.get("expiryDateS2") != null) {
				datePattern = rulesMap.get("expiryDateS2").getFieldFormat();
			} else {
				datePattern = translationDateValidator.determineDateFormat(fileItem.getItem().getExpiryDateS2());
			}
			entity.setExpiryDateS2(LocalDateTime.parse(fileItem.getItem().getExpiryDateS2(),
					DateTimeFormatter.ofPattern(datePattern)));
		}
		if (fileItem.getItem().getExpiryDateS3() != null && !fileItem.getItem().getExpiryDateS3().isEmpty()) {
			String datePattern = "";
			if (rulesMap.get("expiryDateS3") != null) {
				datePattern = rulesMap.get("expiryDateS3").getFieldFormat();
			} else {
				datePattern = translationDateValidator.determineDateFormat(fileItem.getItem().getExpiryDateS3());
			}
			entity.setExpiryDateS3(LocalDateTime.parse(fileItem.getItem().getExpiryDateS3(),
					DateTimeFormatter.ofPattern(datePattern)));
		}
		if (fileItem.getItem().getClosureDateTt() != null && !fileItem.getItem().getClosureDateTt().isEmpty()) {
			String datePattern = "";
			if (rulesMap.get("closureDateTt") != null) {
				datePattern = rulesMap.get("closureDateTt").getFieldFormat();
			} else {
				datePattern = translationDateValidator.determineDateFormat(fileItem.getItem().getClosureDateTt());
			}
			entity.setClosureDateTt(LocalDateTime.parse(fileItem.getItem().getClosureDateTt(),
					DateTimeFormatter.ofPattern(datePattern)));
		}
		if (fileItem.getItem().getClosureDateTtmweb() != null && !fileItem.getItem().getClosureDateTtmweb().isEmpty()) {
			String datePattern = "";
			if (rulesMap.get("closureDateTtmweb") != null) {
				datePattern = rulesMap.get("closureDateTtmweb").getFieldFormat();
			} else {
				datePattern = translationDateValidator.determineDateFormat(fileItem.getItem().getClosureDateTtmweb());
			}
			entity.setClosureDateTtmweb(LocalDateTime.parse(fileItem.getItem().getClosureDateTtmweb(),
					DateTimeFormatter.ofPattern(datePattern)));
		}
		entity.setTotalFailureTime(new BigDecimal(fileItem.getItem().getTotalFailureTime()));
		entity.setEventDti(fileItem.getItem().getEventDti());
		entity.setUnbundled(fileItem.getItem().getUnbundled());
		entity.setAdditionalServices(fileItem.getItem().getAdditionalServices());
		entity.setSlaTypeS1(fileItem.getItem().getSlaTypeS1());
		entity.setRepairOnThresholdS1(fileItem.getItem().getRepairOnThresholdS1());
		entity.setDelayWorkingDays1(new BigDecimal(fileItem.getItem().getDelayWorkingDaysS1()));
		entity.setDelayCalendarSeconds1(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS1()));
		entity.setSlaTypeS2(fileItem.getItem().getSlaTypeS2());
		entity.setRepairOnThresholdS2(fileItem.getItem().getRepairOnThresholdS2());
		entity.setDelayWorkingDays2(new BigDecimal(fileItem.getItem().getDelayWorkingDaysS2()));
		entity.setDelayCalendarSeconds2(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS2()));
		entity.setSlaTypeS3(fileItem.getItem().getSlaTypeS3());
		entity.setRepairOnThresholdS3(fileItem.getItem().getRepairOnThresholdS3());
		entity.setDelayWorkingDays3(new BigDecimal(fileItem.getItem().getDelayWorkingDaysS3()));
		entity.setDelayCalendarSeconds3(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS3()));
		entity.setServiceType(fileItem.getItem().getServiceProblemDesc());
		entity.setFiberServiceCharacterization(fileItem.getItem().getFiberServiceCharacterization());
		entity.setServiceProvided(fileItem.getItem().getServiceProvided());
		entity.setRepairInR4(fileItem.getItem().getRepairInR4());
		entity.setRepairInR8(fileItem.getItem().getRepairInR8());
		entity.setDelayWorkingDays1OldResolution(
				new BigDecimal(fileItem.getItem().getDelayWorkingDays1OldResolution()));
		entity.setDelayWorkingDays2OldResolution(
				new BigDecimal(fileItem.getItem().getDelayWorkingDays2OldResolution()));
		entity.setDelayWorkingDays3OldResolution(
				new BigDecimal(fileItem.getItem().getDelayWorkingDays3OldResolution()));
		entity.setDelayWorkingDays4OldResolution(
				new BigDecimal(fileItem.getItem().getDelayWorkingDays4OldResolution()));

		entity.setExtensiveCodeTt(fileItem.getItem().getExtensiveCodeTt());
		entity.setDelayHoursTim(fileItem.getTotalHoursDelay());
		if ("0".equalsIgnoreCase(fileItem.getFranchigiaInd())
				&& MeasureUnit.DELAY_WORKING_DAYS.value().equalsIgnoreCase(fileItem.getValorizationRuleMeasureUnit())) {
			entity.setDelayDaysTim(fileItem.getItem().getTotalDelay());
		}

		entity.setFlagMassive1("0");
		entity.setTotalDelay1(fileItem.getItem().getTotalDelay1());

		entity.setTotalDelay2(fileItem.getItem().getTotalDelay2());
		entity.setTotalDelay3(fileItem.getItem().getTotalDelay3());

		if ("S".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS1())) {
			entity.setInSla1("SI");
		} else if ("N".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS1())) {
			entity.setInSla1("NO");
		}

		if ("S".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS2())) {
			entity.setInSla2("SI");
		} else if ("N".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS2())) {
			entity.setInSla2("NO");
		}

		if ("S".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS3())) {
			entity.setInSla3("SI");
		} else if ("N".equalsIgnoreCase(fileItem.getItem().getRepairOnThresholdS3())) {
			entity.setInSla3("NO");
		}

		// Analysis status
		String analysisStatus = "";
		String slaCheck = "";

		if (entity.getInSla1() != null) {
			slaCheck = entity.getInSla1();
		} else if (entity.getInSla2() != null) {
			slaCheck = entity.getInSla2();
		} else {
			slaCheck = entity.getInSla3();
		}

		if (InSlaTypes.SI.value().equals(slaCheck)) {
			analysisStatus = AnalaysisStatus.OK_RATING.value();

		} else if (InSlaTypes.NO.value().equals(slaCheck)) {
			analysisStatus = AnalaysisStatus.OK_RATING.value();

		} else if (InSlaTypes.NC.value().equals(slaCheck)) {
			if (entity.getNotificationStatus() != null && (entity.getNotificationStatus().contains("Solo")
					|| entity.getNotificationStatus().contains("GUI") || entity.getNotificationStatus().contains("via")
					|| entity.getNotificationStatus().contains("email"))) {

				analysisStatus = AnalaysisStatus.NOT_PAYABLE.value();

			} else {

				analysisStatus = AnalaysisStatus.NOT_EVALUABLE.value();
			}

		} else if (InSlaTypes.NN.value().equals(slaCheck)) {
			analysisStatus = AnalaysisStatus.OUT_OF_SCOPE.value();

		} else if (InSlaTypes.NA.value().equals(slaCheck) && "2".equals(entity.getPenaltyCode())) {
			analysisStatus = AnalaysisStatus.NOT_RECONCILIABLE.value();
		}

		LOGGER.error("Penalty handle analaysis status setted status: ", entity);
		entity.setAnalysisStatus(analysisStatus);

		// end analysis status

		// entity.setDelayDaysTim(entity.getTotalDelay());

		// PEN-PROD-226 - update pntPenaltyAggregation - using
		// fileItem.getFranchigiaInd() - DO NOT APPLY
//        if (fileItem.getFranchigiaInd()!=null && "1".equalsIgnoreCase(fileItem.getFranchigiaInd()) ) {
//        	log.debug("Franchigia Ind before reset [{}] the Franchigia Flag", fileItem.getFranchigiaInd());
//    		if (entity.getAggregationId()!= null ) {
//    			Long aggregationId = entity.getAggregationId();
//    			franchigiaPeriodDAO.updateAggregationsUsingAggregationId(aggregationId);
//    			log.debug("Updated aggregationId [{}]", aggregationId);
//    		}
//        }
		entity.setFranchigiaFlag(fileItem.getFranchigiaInd() != null ? fileItem.getFranchigiaInd() : "0");
		log.debug("setPenaltyData - Initial Franchigia Flag [{}]", entity.getFranchigiaFlag());
		entity.setSlaThresholdValue1(fileItem.getSlaThresholdValue1());
		entity.setSlaThresholdValue2(fileItem.getSlaThresholdValue2());
		entity.setSlaThresholdValue3(fileItem.getSlaThresholdValue3());

		entity.setPenaltyAmount1(fileItem.getItem().getPenaltyAmount1());
		entity.setPenaltyAmount2(fileItem.getItem().getPenaltyAmount2());
		entity.setPenaltyAmount3(fileItem.getItem().getPenaltyAmount3());

		entity.setElementInPenalty(fileItem.getValorizationRuleMeasureUnit());
		entity.setPenaltyFrequency(fileItem.getPenaltyFrequency());
		entity.setFranchigiaFrequency(fileItem.getFranchigiaFrequency());

		if (entity.getElementInPenalty() == null) {
			entity.setElementInPenalty("TODO: CHANGE_THIS");
		}

		entity.setCalculationRuleId(fileItem.getCalculationRuleId());
		entity.setValorizationRuleId(fileItem.getValorizationRuleId());
		entity.setRuleCalculationDate(LocalDateTime.now());
		entity.setPenaltyKey(fileItem.getItem().getCodeTt());

		if (entity.getFamily() == null) {
			entity.setFamily(fileItem.getItem().getServiceProblemDesc());
		}

		entity.setOrderTypology(fileItem.getItem().getOrderTypology());

		if (entity.getPrevAcceptedAmount() == null) {
			entity.setPrevAcceptedAmount(fileItem.getPrevAcceptedAmount());
		}
		entity.setFlagResponsibilityCrmOa("0");
		entity.setUpdatedInd(Indicator.FALSE.value());

		entity.setFaultOpeningTime(fileItem.getItem().getFaultOpeningTime());

		if (fileItem.getItem().getDelayCalendarSecondsS1() != null
				&& !fileItem.getItem().getDelayCalendarSecondsS1().isEmpty()) {
			entity.setDelayCalendarHours1(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS1())
					.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
			entity.setDelayCalendarDays1(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS1())
					.divide(new BigDecimal(86400), 0, RoundingMode.HALF_EVEN));
		}
		if (fileItem.getItem().getDelayCalendarSecondsS2() != null
				&& !fileItem.getItem().getDelayCalendarSecondsS2().isEmpty()) {
			entity.setDelayCalendarHours2(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS2())
					.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
			entity.setDelayCalendarDays2(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS2())
					.divide(new BigDecimal(86400), 0, RoundingMode.HALF_EVEN));
		}
		if (fileItem.getItem().getDelayCalendarSecondsS3() != null
				&& !fileItem.getItem().getDelayCalendarSecondsS3().isEmpty()) {
			entity.setDelayCalendarHours3(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS3())
					.divide(new BigDecimal(3600L), 4, RoundingMode.HALF_EVEN));
			entity.setDelayCalendarDays3(new BigDecimal(fileItem.getItem().getDelayCalendarSecondsS3())
					.divide(new BigDecimal(86400), 0, RoundingMode.HALF_EVEN));
		}
		if (fileItem.getItem().getPenaltyAmount() == null) {
			entity.setPenaltyAmount(new BigDecimal(0));
		}

		if (entity.getFranchigiaFlag() == null || "0".equals(entity.getFranchigiaFlag())) {
			entity.setSlaThresholdValue(new BigDecimal(100));
		}

		entity.setManualInterventionReason(fileItem.getItem().getManualInterventionReason());
		entity.setDelayWorkingHours1(entity.getDelayCalendarHours1());
		entity.setDelayWorkingHours2(entity.getDelayCalendarHours2());
		entity.setDelayWorkingHours3(entity.getDelayCalendarHours3());

		// PEN-PROD-204
		entity.setCodiceOrdineOloErrProv(fileItem.getItem().getCodiceOrdineOloErrProv());
		entity.setCodiceOrdineTiErrProv(fileItem.getItem().getCodiceOrdineTiErrProv());
		entity.setSoCodiceServizio(fileItem.getItem().getSoCodiceServizio());
		entity.setSoDescrizioneServizio(fileItem.getItem().getSoDescrizioneServizio());
		entity.setSoCodiceEsito(fileItem.getItem().getSoCodiceEsito());
		entity.setSoDescrizioneEsito(fileItem.getItem().getSoDescrizioneEsito());
		entity.setProjectCode(fileItem.getItem().getCodiceProgetto());
		entity.setPenaleOneStep(fileItem.getItem().getPenaleOneStep());
		// PEN-PROD-204

		// PEN-PROD-204C
		// Transcode FLG_TT_ERR_PROV: NULL or "N" --> "N", "S" --> "S"
		String flgTtErrProv = fileItem.getItem().getFlgTtErrProv();
		if (flgTtErrProv == null) {
			log.debug("FLG_TT_ERR_PROV is null, setting to 'N'");
		} else {
			log.debug("FLG_TT_ERR_PROV is '{}'", flgTtErrProv);
		}
		entity.setFlgTtErrProv((flgTtErrProv == null || "N".equals(flgTtErrProv)) ? "N" : flgTtErrProv);
		// Transcode TIPO_PENALE: NULL or "1" --> "Standard", "2" --> "One Step"
		String tipoPenale = fileItem.getItem().getTipoPenale();
		log.debug("TIPO_PENALE value is '{}'", tipoPenale);
		if ("2".equals(tipoPenale)) {
			log.debug("TIPO_PENALE is '2', setting to 'One Step'");
			entity.setTipoPenale("One Step");
		} else {
			// NULL or "1" or any other value --> "Standard"
			log.debug("TIPO_PENALE is null, '1', or other, setting to 'Standard'");
			entity.setTipoPenale("Standard");
		}
		// PEN-PROD-204C

		LocalDateTime endDate = null;
		if (entity.getClosureDateTt() != null) {
			endDate = entity.getClosureDateTt();
		}

		if (endDate != null) {
			Integer partitionKey;
			if (endDate.getMonthValue() < 10) {
				partitionKey = Integer
						.parseInt(String.valueOf(endDate.getYear()) + "0" + String.valueOf(endDate.getMonthValue()));
			} else {
				partitionKey = Integer
						.parseInt(String.valueOf(endDate.getYear()) + String.valueOf(endDate.getMonthValue()));
			}
			if (entity.getYearMonthPartkey() != partitionKey) {
				fileItem.setPreviousPartitionKey(entity.getYearMonthPartkey());
			}

			if (!entity.getYearMonthPartkey().equals(partitionKey)) {
				fileItem.setPreviousPartitionKey(entity.getYearMonthPartkey());
				entity.setNewPartitionKey(partitionKey);
			}

			// entity.setYearMonthPartkey(partitionKey);
			fileItem.setPartitionKey(partitionKey);

			if (partitionKey < PartitionKeys.OLDEST_AVAILABLE_PARTITION.value()) {
				fileItem.setValid(false);
				fileItem.setErrorCode("ERR08");
				fileItem.setErrorMessage(AnalaysisStatus.MISSING_PARTITION.value());
				// Entities with 0 won't be sent to batch insert/update
				// Special case, these entities would not have valid partition
				// for insert and would break other items in batch insert
				entity.setId(0L);
				log.error(
						"OperaPenaltyFileItemProcessorV2 setPenaltyData Missing partition: {}, for item: {}, entity {}",
						partitionKey, fileItem, entity);
				log.error("This entity won't be loaded into Penalties!");
			}

		}
		if ("13".equals(fileItem.getItem().getSourcePenaltyTypeCode())
				|| "14".equals(fileItem.getItem().getSourcePenaltyTypeCode())) {
			setReconciliationKey(entity, fileItem);
		}

	}

	private void setReconciliationKey(final PenaltyEntity entity,
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) {
		StringBuilder reconciliationKey = new StringBuilder();
		reconciliationKey.append(fileItem.getItem().getSourcePenaltyTypeCode()).append("#")
				.append(fileItem.getItem().getCodeTt()).append("#").append(fileItem.getItem().getSourceAccountCode());
		if (entity != null) {
			entity.setReconciliationKey(reconciliationKey.toString());
		}
		fileItem.getItem().setReconciliationKey(reconciliationKey.toString());
	}

	private void setError(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem,
			final String errorCode, final String errorDescription) {
		fileItem.setValid(false);
		fileItem.setErrorCode(errorCode);
		fileItem.setErrorMessage(errorDescription);
		setReconciliationKey(null, fileItem);

	}

	private FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> validate(
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) {
		try {
			FileLoadUtils.validate(fileItem, messageTranslator);
			if (fileItem.isValid()) {
				validateAccount(fileItem);
			}
			validateManualFlag(fileItem);

			return fileItem;
		} catch (Exception e) {
			fileItem.setValid(false);
			fileItem.setErrorCode("ERR99");
			fileItem.setErrorMessage(e.getMessage());
			setReconciliationKey(null, fileItem);
			LOGGER.error("Processing error:", e);
		}
		return fileItem;
	}

	private void validateManualFlag(final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) {
		if ("F".equalsIgnoreCase(fileItem.getItem().getManualUpdateInd())
				&& fileItem.getItem().getManualInterventionReason() == null) {
			fileItem.setValid(false);
			fileItem.setErrorCode("ERR07");
			fileItem.setErrorMessage(messageTranslator.getMessage("missing.manual.intervention.reason"));
		}
	}

	private FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> validateAccount(
			final FileItemProcessingInfo<OperaAssuranceLoadPenaltyEntity> fileItem) {
		AccountTranscodeSourceCode result = CacheManager.accountTranscodeCache
				.get(fileItem.getItem().getSourceAccountCode());
		if (result == null || result.getAccountId() == null) {
			fileItem.setValid(false);
			fileItem.setErrorCode("ERR04");
			fileItem.setErrorMessage(messageTranslator.getMessage("no.corresponding.account"));
			setReconciliationKey(null, fileItem);
		} else {
			fileItem.setAccountId(result.getAccountId());
			fileItem.setAccountName(result.getAccountName());
			fileItem.setAccountShortCode(result.getAccountShortCode());
			fileItem.setParentAccountId(result.getParentAccountId());
			fileItem.setParentAccountShortCode(result.getParentAccountShortCode());
			fileItem.setValorizationRuleDate(result.getValorizationRuleDate());
			fileItem.setValorizationRegistrationDate(result.getValorizationRegistrationDate());
			fileItem.getItem().setSourceAccountCode(result.getMasterAccountId());
		}
		return fileItem;
	}

	private void createOperaRequestJpubPrice(final OperaAssuranceLoadPenaltyEntity fileItem,
			final RequestJpubPrice request, final List<ServiceTypeTranscodeJpubEntity> regulatedJpubMap,
			final List<ServiceTypeTranscodeJpubEntity> nonRegulatedJpubMap) throws ParseException {
		String serviceType = fileItem.getServiceProblemDesc() + ";" + fileItem.getServiceType() + ";"
				+ fileItem.getOaoTicketType() + ";" + fileItem.getFiberServiceCharacterization();
		request.setLoadTableId(fileItem.getId());
		request.setPenaltyType(fileItem.getSourcePenaltyTypeCode());
		request.setRequestCode(fileItem.getCodeTt());
		request.setServiceType(serviceType);
		request.setResourceId(fileItem.getResourceIdTd());
		request.setSourceAccountCode(fileItem.getSourceAccountCode());
		request.setPriceReferenceDate(LocalDateTime.parse(fileItem.getClosureDateTt(), DateTimeFormatter
				.ofPattern(translationDateValidator.determineDateFormat(fileItem.getClosureDateTt()))));
		request.setResponseType("1");
		request.setSourceSystemCode("OPERA");
		request.setSourceAccountCode(fileItem.getSourceAccountCode());
		request.setPenaltyKey(generatePenaltyCode(fileItem, fileItem.getCodeTt()));
		ServiceTypeTranscodeJpubRequest serviceTypeJpubRequest = new ServiceTypeTranscodeJpubRequest();
		serviceTypeJpubRequest.setScope(fileItem.getScope());
		serviceTypeJpubRequest.setServiceTypeRegulated(fileItem.getServiceType());
		serviceTypeJpubRequest.setServiceTypeNonRegulated(fileItem.getServiceType());
		serviceTypeJpubRequest.setServiceProblemDesc(fileItem.getServiceProblemDesc());
		serviceTypeJpubRequest.setOaoTicketType(fileItem.getOaoTicketType());
		serviceTypeJpubRequest.setFiberServiceCharacterization(fileItem.getFiberServiceCharacterization());
		if (LoadPenaltyScopeType.REGULATED.value().equals(serviceTypeJpubRequest.getScope())) {
			ServiceTypeTranscodeJpubEntity regulatedJpub = regulatedJpubMap.stream()
					.filter(r -> checkRegulatedKey(r, serviceTypeJpubRequest)).findFirst().orElse(null);

			request.setServiceTypeJpub(regulatedJpub == null ? null : regulatedJpub.getJpubCode());

		} else {
			ServiceTypeTranscodeJpubEntity nonRegulatedJpub = nonRegulatedJpubMap.stream()
					.filter(r -> checkNonRegulatedKey(r, serviceTypeJpubRequest)).findFirst().orElse(null);

			request.setServiceTypeJpub(nonRegulatedJpub == null ? null : nonRegulatedJpub.getJpubCode());
		}
	}

	public Boolean checkRegulatedKey(final ServiceTypeTranscodeJpubEntity key,
			final ServiceTypeTranscodeJpubRequest r) {
		return (key.getScope() == null || r.getScope().equals(key.getScope()))
				&& (key.getServiceTypeRegulated() == null
						|| r.getServiceTypeRegulated().equals(key.getServiceTypeRegulated()))
				&& (key.getServiceProblemDesc() == null
						|| r.getServiceProblemDesc().equals(key.getServiceProblemDesc()))
				&& (key.getOaoTicketType() == null || r.getOaoTicketType().equals(key.getOaoTicketType()))
				&& (key.getFiberServiceCharacterization() == null
						|| r.getFiberServiceCharacterization().equals(key.getFiberServiceCharacterization()));

	}

	public Boolean checkNonRegulatedKey(final ServiceTypeTranscodeJpubEntity key,
			final ServiceTypeTranscodeJpubRequest r) {
		return (key.getScope() == null || r.getScope().equals(key.getScope()))
				&& (key.getServiceTypeNonregulated() == null
						|| r.getServiceTypeNonRegulated().equals(key.getServiceTypeNonregulated()))
				&& (key.getServiceProblemDesc() == null
						|| r.getServiceProblemDesc().equals(key.getServiceProblemDesc()))
				&& (key.getOaoTicketType() == null || r.getOaoTicketType().equals(key.getOaoTicketType()))
				&& (key.getFiberServiceCharacterization() == null
						|| r.getFiberServiceCharacterization().equals(key.getFiberServiceCharacterization()));
	}

	private String generateOpereRegulatedKey(final ServiceTypeTranscodeJpubRequest r) {
		StringBuilder key = new StringBuilder();
		key.append(r.getScope() != null ? r.getScope() : "-");
		key.append(r.getServiceTypeRegulated() != null ? r.getServiceTypeRegulated() : "-");
		key.append(r.getServiceProblemDesc() != null ? r.getServiceProblemDesc() : "-");
		key.append(r.getOaoTicketType() != null ? r.getOaoTicketType() : "-");
		key.append(r.getFiberServiceCharacterization() != null ? r.getFiberServiceCharacterization() : "-");
		return key.toString();
	}

	private String generateOpereNonRegulatedKey(final ServiceTypeTranscodeJpubRequest r) {
		StringBuilder key = new StringBuilder();
		key.append(r.getScope() != null ? r.getScope() : "-");
		key.append(r.getServiceTypeNonRegulated() != null ? r.getServiceTypeNonRegulated() : "-");
		key.append(r.getServiceProblemDesc() != null ? r.getServiceProblemDesc() : "-");
		key.append(r.getOaoTicketType() != null ? r.getOaoTicketType() : "-");
		key.append(r.getFiberServiceCharacterization() != null ? r.getFiberServiceCharacterization() : "-");
		return key.toString();
	}

	private void updateCancelled(final OperaAssuranceLoadPenaltyEntity entity) {
		entity.setPenaltyAmount(new BigDecimal("0"));
		entity.setSlaThresholdValue1(null);
		entity.setTotalDelay1(null);
		entity.setStatus(FileLoadItemsPenaltyStatus.CANCELLED.value());
		entity.setModified(LocalDateTime.now());
		// entity.setModifiedBy("system");
		LOGGER.debug("Cancelled because of 'Telecom Italia'");
	}

	private String generatePenaltyCode(final OperaAssuranceLoadPenaltyEntity fileItem, final String code) {
		String penaltyKey = "";
		if ("12".equals(fileItem.getSourcePenaltyTypeCode())) {
			// TODO add when new opera added
		} else if ("13".equals(fileItem.getSourcePenaltyTypeCode())
				|| "14".equals(fileItem.getSourcePenaltyTypeCode())) {
			penaltyKey = fileItem.getSourcePenaltyTypeCode() + "#" + fileItem.getCodeTt() + "#"
					+ fileItem.getSourceAccountCode();
			// TODO missing order codes
		}

		return penaltyKey;

	}

	@Override
	public List<OperaAssuranceLoadPenaltyEntity> getItems(final long fileHeadId, final Integer partKey, final int page,
			final int size) {
		return operaAssuranceLoadPenaltyDAO.getQueuedItemsV2(fileHeadId, partKey, page, size);
	}

	@Override
	public void updateCounts(final Long fileHeadId, final Integer partKey) {
		fileLoadHeadPenaltyDAO.updateCountsV2(fileHeadId, partKey,
				OperaAssuranceLoadPenaltyEntity.class.getSimpleName(), "system");
	}

	@Override
	public void updateOldDiscardDuplicates(final Long fileHeadId, final Integer partitionKey, final Long processLogId,
			final String sourcePenaltyTypeCode) {
		RegistryPenaltyEntity penalty = registryPenaltyDAO.getByFileNamePrefix(sourcePenaltyTypeCode);
		operaAssuranceLoadPenaltyDAO.updateOperaAssuranceDuplicatesV2(fileHeadId, partitionKey, processLogId,
				penalty.getCode());
	}

	private List<ServiceTypeTranscodeJpubEntity> getOperaServiceTypeJpubRegulatedMap() {

		return serviceTypeTranscodeJpubDAO.getOperaServiceTypeJpubRegulatedMapV2();

	}

	private List<ServiceTypeTranscodeJpubEntity> getOperaServiceTypeJpubNonRegulatedMap() {

		return serviceTypeTranscodeJpubDAO.getOperaServiceTypeJpubNonRegulatedMapV2();

	}

	@Override
	public void updateCreateAggregation(final Long id, final Integer partitionKey) {
		List<PenaltyEntity> penalties = penaltyDAO.getByLoadFileReconKey(id, partitionKey);
		handleFranchigiaPeriod(penalties);
	}

	private void handlePenaltyTypology(final List<OperaAssuranceLoadPenaltyEntity> fileItems,
			final List<PenaltyEntity> penalties) {
		LOGGER.debug("START: handlePenaltyTypology : {}, {} ", fileItems, penalties);
		for (OperaAssuranceLoadPenaltyEntity opera : fileItems) {
			opera.setPenaltyTypology(opera.getSourcePenaltyTypeCode() != null
					? operaAssuranceLoadPenaltyDAO.getPenaltyTypology(opera.getSourcePenaltyTypeCode())
					: "");
		}
		for (PenaltyEntity penalty : penalties) {
			penalty.setPenaltyTypology(
					penalty.getPenaltyCode() != null ? penaltyDAO.getPenaltyTypology(penalty.getPenaltyCode()) : "");
		}

		LOGGER.debug("END: handlePenaltyTypology : {}, {} ", fileItems, penalties);
	}

	private void updateToNewPartitionKeys(final List<PenaltyEntity> penalties) {
		for (PenaltyEntity penalty : penalties) {
			if (penalty.getNewPartitionKey() != 0) {
				penaltyMatchedItemDAO.updatePenaltyPartitionKeyOnMatched(penalty);
				penaltyDAO.updatePartitionKey(penalty);
			}
		}
	}

}