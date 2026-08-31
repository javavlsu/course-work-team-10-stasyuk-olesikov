(function () {
    const GAME_ID = window.__GAME_ID__ || "unknown";
    const GAMEVER_ID = window.__GAMEVER_ID__ || "unknown";

    const PREFIX = `game_${GAME_ID}_ver_${GAMEVER_ID}_`;

    const realStorage = window.localStorage;

    function fullKey(key) {
        return PREFIX + key;
    }

    function getNamespacedKeys() {
        const keys = [];

        for (let i = 0; i < realStorage.length; i++) {
            const k = realStorage.key(i);

            if (k && k.startsWith(PREFIX)) {
                keys.push(k);
            }
        }

        return keys;
    }

    const namespacedStorage = {
        getItem(key) {
            return realStorage.getItem(fullKey(key));
        },

        setItem(key, value) {
            return realStorage.setItem(fullKey(key), value);
        },

        removeItem(key) {
            return realStorage.removeItem(fullKey(key));
        },

        clear() {
            const keys = getNamespacedKeys();

            for (const k of keys) {
                realStorage.removeItem(k);
            }
        },

        key(index) {
            const keys = getNamespacedKeys();

            const key = keys[index];

            if (!key) {
                return null;
            }

            return key.substring(PREFIX.length);
        },

        get length() {
            return getNamespacedKeys().length;
        }
    };

    Object.defineProperty(window, "localStorage", {
        value: namespacedStorage,
        configurable: false,
        writable: false
    });
})();