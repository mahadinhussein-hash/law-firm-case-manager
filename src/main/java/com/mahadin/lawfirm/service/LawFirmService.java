package com.mahadin.lawfirm.service;

import com.mahadin.lawfirm.domain.Client;
import com.mahadin.lawfirm.domain.CorporateCase;
import com.mahadin.lawfirm.domain.CriminalCase;
import com.mahadin.lawfirm.domain.FamilyCase;
import com.mahadin.lawfirm.domain.Lawyer;
import com.mahadin.lawfirm.domain.LegalCase;
import com.mahadin.lawfirm.exception.CaseLoadExceededException;
import com.mahadin.lawfirm.exception.ConflictOfInterestException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LawFirmService {

    private final List<Client> clients = new ArrayList<>();
    private final List<Lawyer> lawyers = new ArrayList<>();
    private final List<LegalCase> cases = new ArrayList<>();

    private int nextClientId = 1;
    private int nextLawyerId = 1;
    private int nextCaseId = 1;

    public Client addClient(String name, String email, String phone) {
        Client client = new Client(nextClientId, name, email, phone);
        clients.add(client);
        nextClientId++;
        return client;
    }

    public Lawyer addLawyer(String name, double hourlyRate) {
        Lawyer lawyer = new Lawyer(nextLawyerId, name, hourlyRate);
        lawyers.add(lawyer);
        nextLawyerId++;
        return lawyer;
    }

    public LegalCase openCriminalCase(String title, Client client, String opposingParty,
                                      LocalDate deadline, Lawyer lawyer, String charge)
            throws CaseLoadExceededException, ConflictOfInterestException {
        checkCanAssign(lawyer, opposingParty, null);
        return register(new CriminalCase(nextCaseId, title, client, opposingParty, deadline, lawyer, charge));
    }

    public LegalCase openFamilyCase(String title, Client client, String opposingParty,
                                    LocalDate deadline, Lawyer lawyer, String matterType)
            throws CaseLoadExceededException, ConflictOfInterestException {
        checkCanAssign(lawyer, opposingParty, null);
        return register(new FamilyCase(nextCaseId, title, client, opposingParty, deadline, lawyer, matterType));
    }

    public LegalCase openCorporateCase(String title, Client client, String opposingParty,
                                       LocalDate deadline, Lawyer lawyer, String companyName)
            throws CaseLoadExceededException, ConflictOfInterestException {
        checkCanAssign(lawyer, opposingParty, null);
        return register(new CorporateCase(nextCaseId, title, client, opposingParty, deadline, lawyer, companyName));
    }

    public void assignLawyer(LegalCase legalCase, Lawyer lawyer)
            throws CaseLoadExceededException, ConflictOfInterestException {
        checkCanAssign(lawyer, legalCase.getOpposingParty(), legalCase);
        legalCase.setLeadLawyer(lawyer);
    }

    public int countActiveCases(Lawyer lawyer) {
        int count = 0;
        for (LegalCase legalCase : cases) {
            if (legalCase.isActive() && legalCase.getLeadLawyer().equals(lawyer)) {
                count++;
            }
        }
        return count;
    }

    public List<Client> getClients() {
        return Collections.unmodifiableList(clients);
    }

    public List<Lawyer> getLawyers() {
        return Collections.unmodifiableList(lawyers);
    }

    public List<LegalCase> getCases() {
        return Collections.unmodifiableList(cases);
    }

    private LegalCase register(LegalCase legalCase) {
        cases.add(legalCase);
        nextCaseId++;
        return legalCase;
    }

    private void checkCanAssign(Lawyer lawyer, String opposingParty, LegalCase caseBeingChanged)
            throws CaseLoadExceededException, ConflictOfInterestException {
        if (lawyer == null) {
            throw new IllegalArgumentException("A lawyer must be chosen.");
        }

        int activeCases = countActiveCases(lawyer);
        if (caseBeingChanged != null && caseBeingChanged.isActive()
                && lawyer.equals(caseBeingChanged.getLeadLawyer())) {
            activeCases--;
        }
        if (activeCases >= Lawyer.MAX_ACTIVE_CASES) {
            throw new CaseLoadExceededException(lawyer.getName() + " already has "
                    + Lawyer.MAX_ACTIVE_CASES + " active cases.");
        }

        if (opposingParty != null) {
            for (LegalCase legalCase : cases) {
                boolean sameLawyer = legalCase.isActive() && legalCase.getLeadLawyer().equals(lawyer);
                boolean opposingIsOwnClient = legalCase.getClient().getName().equalsIgnoreCase(opposingParty.trim());
                if (sameLawyer && opposingIsOwnClient) {
                    throw new ConflictOfInterestException(lawyer.getName() + " already represents "
                            + opposingParty.trim() + " and cannot act against them.");
                }
            }
        }
    }
}
