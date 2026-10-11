package com.example.englishaicoach.feature.onboarding;

import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.repository.OnboardingRepository;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import com.example.englishaicoach.domain.usecase.ConfigureOnboardingUseCase;
import org.junit.*;
import java.util.*;

public final class OnboardingViewModelTest {
    static final String ID="00000000-0000-0000-0000-000000000001";
    Fake repository;SessionManager sessions;OnboardingViewModel model;
    @Before public void setup(){
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor(){public void executeOnDiskIO(Runnable r){r.run();}public void postToMainThread(Runnable r){r.run();}public boolean isMainThread(){return true;}});
        sessions=new SessionManager(new TokenStore(){TokenSession value;public TokenSession read(){return value;}public void write(TokenSession v){value=v;}});sessions.start("a","r");
        repository=new Fake();model=new OnboardingViewModel(new ConfigureOnboardingUseCase(repository),sessions,Runnable::run);
    }
    @After public void cleanup(){model.onCleared();ArchTaskExecutor.getInstance().setDelegate(null);}
    Snapshot snapshot(){return new Snapshot(new Profile("Tên",20,"UTC","avatar"),List.of(new Goal(ID,"Du lịch",true)),new Selection(List.of(ID),null));}
    void loaded(){model.load();repository.callbacks.get(0).completed(new OnboardingRepository.Result(snapshot(),true,false,null));}
    void partial(){repository.callbacks.get(repository.callbacks.size()-1).completed(new OnboardingRepository.Result(null,false,true,new ApiErrorParser().parse(400,null)));}
    @Test public void existingProfilePrefillsAndRecreationLoadDoesNotOverwriteDraft(){loaded();model.name="Tên mới";model.load();assertEquals(1,repository.callbacks.size());assertEquals("Tên mới",model.name);assertTrue(model.zoneConfirmed);}
    @Test public void missingProfileRequiresExplicitTimeAndZone(){model.load();repository.callbacks.get(0).completed(new OnboardingRepository.Result(new Snapshot(new Profile("Tên",null,null,null),List.of(new Goal(ID,"Goal",true)),new Selection(List.of(),null)),true,false,null));assertEquals("",model.zone);assertEquals("",model.minutes);assertFalse(model.zoneConfirmed);model.save(true);assertEquals(0,repository.saves);}
    @Test public void optionalPrimaryAndExactMinutesBounds(){loaded();for(String value:List.of("5","180")){model.minutes=value;assertNotNull(model.parsedMinutes());}for(String value:List.of("4","181","20.5"," 20","")){model.minutes=value;assertNull(model.parsedMinutes());}assertTrue(model.validGoals());model.primary="foreign";assertFalse(model.validGoals());}
    @Test public void inactiveSelectionCannotBeSilentlySent(){model.load();repository.callbacks.get(0).completed(new OnboardingRepository.Result(new Snapshot(snapshot().profile,List.of(new Goal(ID,"Cũ",false)),snapshot().selection),true,false,null));assertFalse(model.validGoals());model.save(true);assertEquals(0,repository.saves);}
    @Test public void offlineAndDuplicateBlockMutation(){loaded();model.save(false);assertEquals(0,repository.saves);model.save(true);model.save(true);assertEquals(1,repository.saves);assertEquals("avatar",repository.profile.avatarUrl);}
    @Test public void partialRetrySkipsOnlyUnchangedConfirmedProfile(){loaded();model.save(true);assertFalse(repository.skip);partial();model.save(true);assertTrue(repository.skip);partial();model.name="Tên khác";model.save(true);assertFalse(repository.skip);}
    @Test public void sessionSwitchWipesPrivateDraftAndDropsLateSave(){loaded();model.save(true);var late=repository.callbacks.get(1);sessions.start("b","s");assertEquals("",model.name);assertEquals(0,model.selected.size());assertEquals(OnboardingViewModel.Phase.EXPIRED,model.state().getValue().phase);late.completed(new OnboardingRepository.Result(null,true,true,null));assertEquals(OnboardingViewModel.Phase.EXPIRED,model.state().getValue().phase);assertEquals(1,repository.cancels);}
    @Test public void queuedResultChecksSessionAtPublication(){model.onCleared();List<Runnable> queue=new ArrayList<>();model=new OnboardingViewModel(new ConfigureOnboardingUseCase(repository),sessions,queue::add);model.load();repository.callbacks.get(0).completed(new OnboardingRepository.Result(snapshot(),true,false,null));sessions.start("b","s");for(Runnable task:queue)task.run();assertEquals(OnboardingViewModel.Phase.EXPIRED,model.state().getValue().phase);assertNull(model.state().getValue().snapshot);}
    @Test public void clearCancelsAndRejectsLateLoad(){model.load();model.onCleared();repository.callbacks.get(0).completed(new OnboardingRepository.Result(snapshot(),true,false,null));assertEquals(OnboardingViewModel.Phase.LOADING,model.state().getValue().phase);assertEquals(1,repository.cancels);}
    static final class Fake implements OnboardingRepository {
        List<Callback> callbacks=new ArrayList<>();int saves,cancels;Profile profile;boolean skip;
        public Cancelable load(Callback callback){callbacks.add(callback);return ()->cancels++;}
        public Cancelable save(Profile p,Selection s,boolean saved,Callback callback){profile=p;skip=saved;saves++;callbacks.add(callback);return ()->cancels++;}
    }
}
