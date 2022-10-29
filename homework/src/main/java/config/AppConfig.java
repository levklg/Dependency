package config;


import appcontainer.api.AppComponent;
import appcontainer.api.AppComponentsContainerConfig;
import services.*;

@AppComponentsContainerConfig(order = 1)
public class AppConfig {

    @AppComponent(order = 1, name = "equationPreparer")
    public EquationPreparer equationPreparer(){
        return new EquationPreparerImpl();
    }

    @AppComponent(order = 2, name = "playerService")
    public PlayerService playerService(IOService ioService) {
        return new PlayerServiceImpl(ioService);
    }

    @AppComponent(order = 3, name = "gameProcessor")
    public GameProcessor gameProcessor(IOService ioService,
                                       EquationPreparer equationPreparer,
                                       PlayerService playerService
                                       ) {
        return new GameProcessorImpl(ioService,equationPreparer,playerService );
    }

    @AppComponent(order = 0, name = "ioService")
    public IOService ioService() {
        return new IOServiceStreams(System.out, System.in);
    }

}
