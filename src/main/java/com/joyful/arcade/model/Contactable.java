package com.joyful.arcade.model;

import java.util.function.Function;

public interface Contactable extends Sizeable {
    void resolveContact(Contactable with);

}
