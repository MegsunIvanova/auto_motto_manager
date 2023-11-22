package bg.softuni.autho_moto_manager.service.impl;

import bg.softuni.autho_moto_manager.model.dto.binding.CreateVehicleDTO;
import bg.softuni.autho_moto_manager.model.dto.binding.SaleDTO;
import bg.softuni.autho_moto_manager.model.dto.view.*;
import bg.softuni.autho_moto_manager.model.entity.CostEntity;
import bg.softuni.autho_moto_manager.model.entity.ModelEntity;
import bg.softuni.autho_moto_manager.model.entity.SaleEntity;
import bg.softuni.autho_moto_manager.model.entity.VehicleEntity;
import bg.softuni.autho_moto_manager.model.enums.CostTypeEnum;
import bg.softuni.autho_moto_manager.repository.CostRepository;
import bg.softuni.autho_moto_manager.repository.CurrencyRepository;
import bg.softuni.autho_moto_manager.repository.MakeRepository;
import bg.softuni.autho_moto_manager.repository.VehicleRepository;
import bg.softuni.autho_moto_manager.service.VehicleService;
import bg.softuni.autho_moto_manager.service.exceptions.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static bg.softuni.autho_moto_manager.util.Constants.BLANK_AUTOMOBILE_IMG_SRC;
import static bg.softuni.autho_moto_manager.util.Constants.BLANK_MOTORCYCLE_IMG_SRC;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final MakeRepository makeRepository;
    private final VehicleRepository vehicleRepository;
    private final CostRepository costRepository;
    private final CurrencyRepository currencyRepository;
    private final ModelMapper modelMapper;

    public VehicleServiceImpl(MakeRepository makeRepository,
                              VehicleRepository vehicleRepository,
                              CostRepository costRepository, CurrencyRepository currencyRepository, ModelMapper modelMapper) {
        this.makeRepository = makeRepository;
        this.vehicleRepository = vehicleRepository;
        this.costRepository = costRepository;
        this.currencyRepository = currencyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AddVehicleViewDTO getAddVehicleView() {
        return new AddVehicleViewDTO(getModelsByMake());
    }

    @Override
    public void create(CreateVehicleDTO createVehicleDTO) {
        VehicleEntity vehicleEntity = modelMapper.map(createVehicleDTO, VehicleEntity.class);
        vehicleRepository.save(vehicleEntity);
    }

    @Override
    @Transactional
    public Page<VehicleSummaryViewDTO> getAllVehicles(Pageable page) {
        return vehicleRepository
                .findAll(page)
                .map(VehicleSummaryViewDTO::new);
    }


    @Override
    @Transactional
    public VehicleDetailsViewDTO getDetailsByUuid(String uuid) {
        VehicleEntity vehicleEntity = vehicleRepository.findByUuid(uuid)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Vehicle with uuid " + uuid + " can not be found!"));

        List<PictureViewDTO> pictureViewDTOS = vehicleEntity
                .getPictures()
                .stream()
                .map(PictureViewDTO::new)
                .toList();

        Map<CostTypeEnum, BigDecimal> totalCostsByType = costRepository
                .findAllByVehicle_Uuid(uuid)
                .stream().collect(Collectors.groupingBy(
                        CostEntity::getType,
                        () -> new TreeMap<>(Comparator.comparingInt(CostTypeEnum::ordinal)),
                        Collectors.reducing(BigDecimal.ZERO,
                                CostEntity::getAmountInBGN,
                                BigDecimal::add)));

        SaleViewDTO sale = vehicleEntity.getSale() == null
                ? null
                : modelMapper.map(vehicleEntity.getSale(), SaleViewDTO.class);

        return new VehicleDetailsViewDTO(vehicleEntity, pictureViewDTOS, totalCostsByType, sale);
    }

    private Map<String, List<String>> getModelsByMake() {
        Map<String, List<String>> modelsByMake = new LinkedHashMap<>();

        makeRepository.findAllOrdered()
                .forEach(make -> modelsByMake.put(
                        make.getName(),
                        make.getModels().stream().map(ModelEntity::getName).toList()));

        return modelsByMake;
    }

    protected static String primaryImgSrc(VehicleEntity vehicleEntity) {
        if (vehicleEntity.getPrimaryImage() == null) {
            return switch (vehicleEntity.getModel().getType()) {
                case AUTOMOBILE -> BLANK_AUTOMOBILE_IMG_SRC;
                case MOTORCYCLE -> BLANK_MOTORCYCLE_IMG_SRC;
                default -> "";
            };
        }

        return vehicleEntity.getPrimaryImage().getUrl();
    }
}

