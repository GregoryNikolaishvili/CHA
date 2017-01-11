package ge.altasoft.gia.cha;

public abstract class RunnableWithParams implements Runnable {

    private String m_parameter;

    public RunnableWithParams() {
    }

    public void run(final String parameter) {
        this.m_parameter = parameter;
        run();
    }

    public String getParameter() {
        return this.m_parameter;
    }
}
