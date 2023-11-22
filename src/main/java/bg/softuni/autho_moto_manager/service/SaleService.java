package bg.softuni.autho_moto_manager.service;

import bg.softuni.autho_moto_manager.model.dto.binding.SaleDTO;
import bg.softuni.autho_moto_manager.model.dto.view.SaleVehicleView;

public interface SaleService {

    SaleVehicleView getSaleVehicleView(String uuid);

    void sell(SaleDTO saleDTO);
}
