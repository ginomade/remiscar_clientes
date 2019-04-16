package com.nomade.forma.app.events;

public class MainViewEvent {
    private String content;
    private String reserva;
    private String empresa;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReserva() {
        return reserva;
    }

    public void setReserva(String reserva) {
        this.reserva = reserva;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }
}
