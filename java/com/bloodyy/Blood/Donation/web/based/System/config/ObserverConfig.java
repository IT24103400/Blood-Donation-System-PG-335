package com.bloodyy.Blood.Donation.web.based.System.config;

import com.bloodyy.Blood.Donation.web.based.System.observer.BloodRequestObserver;
import com.bloodyy.Blood.Donation.web.based.System.observer.DonorNotificationObserver;
import com.bloodyy.Blood.Donation.web.based.System.observer.MedicalStaffAlertObserver;
import com.bloodyy.Blood.Donation.web.based.System.service.HospitalStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class ObserverConfig {

    @Autowired
    private HospitalStaffService hospitalStaffService;

    @Autowired
    private DonorNotificationObserver donorNotificationObserver;

    @Autowired
    private MedicalStaffAlertObserver medicalStaffAlertObserver;

    @PostConstruct
    public void registerObservers() {
        System.out.println("=== REGISTERING OBSERVERS ===");

        // Register the observers
        hospitalStaffService.registerObserver(donorNotificationObserver);
        hospitalStaffService.registerObserver(medicalStaffAlertObserver);

        // Print registered observers for confirmation
        System.out.println("Registered observers: " + hospitalStaffService.getRegisteredObservers());
        System.out.println("=== OBSERVERS REGISTERED SUCCESSFULLY ===");
    }
}