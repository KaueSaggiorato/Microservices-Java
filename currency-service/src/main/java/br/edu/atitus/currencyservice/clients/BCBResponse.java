package br.edu.atitus.currencyservice.clients;

import java.util.List;

public record BCBResponse(List<BCBRate> value) {

    public record BCBRate(String cotacaoVenda) {}
}