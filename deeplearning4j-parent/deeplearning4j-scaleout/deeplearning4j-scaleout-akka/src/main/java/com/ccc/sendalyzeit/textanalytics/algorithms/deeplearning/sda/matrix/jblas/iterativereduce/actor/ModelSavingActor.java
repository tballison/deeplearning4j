package com.ccc.sendalyzeit.textanalytics.algorithms.deeplearning.sda.matrix.jblas.iterativereduce.actor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.ccc.sendalyzeit.textanalytics.ml.scaleout.iterativereduce.jblas.UpdateableMatrix;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;


/**
 * Listens for a neural network to save
 * @author Adam Gibson
 *
 */
public class ModelSavingActor extends UntypedActor {

	public final static String SAVE = "save";
	private String pathToSave;
	private ActorRef mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


	public ModelSavingActor(String pathToSave) {
		this.pathToSave = pathToSave;
	}


	{
		mediator.tell(new DistributedPubSubMediator.Subscribe(SAVE, getSelf()), getSelf());
	}


	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof UpdateableMatrix) {
			UpdateableMatrix u = (UpdateableMatrix) message;
			File save = new File(pathToSave);
			if(save.exists()) {
				File parent = save.getParentFile();
				save.renameTo(new File(parent,save.getName() + "-" + System.currentTimeMillis()));
			}
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(save));
			u.get().write(bos);
			bos.flush();
			bos.close();
			log.info("saved model to " + pathToSave);
		}
	}


	public static class ModelSavingActorFactory implements Creator<ModelSavingActor> {

		public ModelSavingActorFactory(String path) {
			this.path = path;
		}
		private String path;


		@Override
		public ModelSavingActor create() throws Exception {
			return new ModelSavingActor(path);
		}

	}


}
