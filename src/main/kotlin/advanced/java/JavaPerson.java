package advanced.java;

import org.jetbrains.annotations.NotNull;

public class JavaPerson {

    @NotNull
    private String name;
    private int age;

    public JavaPerson(@NotNull String name, int age) {
        super();
        this.name = name;
        this.age = age;
    }

    public final boolean isMature() {
        return this.age > 18;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final void setName(@NotNull String name) {
        this.name = name;
    }

    public final int getAge() {
        return this.age;
    }

    public final void setAge(int var1) {
        this.age = var1;
    }
}
