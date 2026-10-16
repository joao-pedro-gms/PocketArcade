package br.com.joaop.pocketarcade;

final class AppContract {
    static final String EXTRA_NICKNAME = "br.com.joaop.pocketarcade.NICKNAME";
    static final String EXTRA_MODULE = "br.com.joaop.pocketarcade.MODULE";
    static final String MODULE_SCANNER = "Scanner de Mensagens";
    static final String MODULE_ARENA = "Arena";
    static final String MODULE_VAULT = "Código Secreto";
    static final String MODULE_RANDOMIZER = "Randomizador";

    static boolean isKnownModule(String module) {
        return MODULE_SCANNER.equals(module)
                || MODULE_ARENA.equals(module)
                || MODULE_VAULT.equals(module)
                || MODULE_RANDOMIZER.equals(module);
    }

    static boolean isValidNickname(String nickname) {
        if (nickname == null) return false;
        String normalized = nickname.trim();
        return normalized.codePointCount(0, normalized.length()) >= 3;
    }

    private AppContract() { }
}
