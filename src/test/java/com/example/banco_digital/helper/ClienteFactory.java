package com.example.banco_digital.helper;

public final class ClienteFactory {

    public static String gerarCpf() {
        long n = Math.abs(System.nanoTime() % 100_000_000_000L);
        return String.format("%011d", n);
    }
}