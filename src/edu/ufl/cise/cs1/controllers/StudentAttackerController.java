package edu.ufl.cise.cs1.controllers;

import game.controllers.AttackerController;
import game.models.*;

import java.util.ArrayList;
import java.util.List;

public final class StudentAttackerController implements AttackerController
{

	private int mode;

	public void init(Game game)
	{
		mode = 1;
	}

	public void shutdown(Game game) { }

	public int update(Game game,long timeDue)
	{

		List<Node> prey = new ArrayList<>();
		List<Node> predators = new ArrayList<>();

		Attacker myGuy = game.getAttacker();

		//let's start off by looping through every defender in the game constantly
		for (Defender defender: game.getDefenders())
		{
			//if they're vulnerable we'll call them prey and add them to that list
			if (defender.isVulnerable())
			{
				prey.add(defender.getLocation());
			}
			//if they're not then they're predators
			else
			{
				predators.add(defender.getLocation());
			}
		}

		/*
		So our strategy is to have myGuy go to the nearest power pill, wait for predators to come close and ambush them
		by eating the pill and chasing them after they've become vulnerable.
		We're gonna split every state of myGuy's behavior into "modes" as it figures out what's the best option to take.
		There are several flaws in the process but it's simple enough and does the trick.
		 */


		System.out.println(mode);

		if (mode == 1) //To Power Pill
		{

			//if there are no power pills left, we don't want the prog to crash
			if(game.getPowerPillList().isEmpty())
			{
				//so we either chase prey if there are any vulnerable defenders left
				if (prey.size() > 0)
				{
					mode = 4;
				}
				//and if there aren't we go into trying to eat as many regular pills as possible
				else
				{
					mode = 5;
					return -1;
				}
			}

			//if myGuy is really close to a power pill then he'll go into Waiting Mode
			if (myGuy.getLocation().getPathDistance(myGuy.getTargetNode(game.getPowerPillList(), true)) <= 2)
			{
				mode = 2;
			}

			//if the above conditions aren't the case then myGuy just goes toward the nearest power pill
			Node closestPowerPill = myGuy.getTargetNode(game.getPowerPillList(), true);
			return myGuy.getNextDir(closestPowerPill, true);
		}



		if (mode == 2) //Waiting Mode
		{
			//we have to define a distance between myGuy and predators
			int distance = myGuy.getLocation().getPathDistance(myGuy.getTargetNode(predators, true));

			//switch to Ambush Mode when the predator gets real close
			if (distance < 5  && distance >= 1)
			{
				mode = 3;
			}

			//do a little dance while you wait for the predators to come
			return myGuy.getReverse();
		}



		if (mode == 3) //Ambush Mode
		{

			//if there are any vulnerable defenders in the maze, switch to Chase Mode
			if (prey.size() > 0)
			{
				mode = 4;
			}

			//if there aren't power pills left...
			if(game.getPowerPillList().isEmpty())
			{

				//switch to chasing if there are prey left
				if (prey.size() > 0)
				{
					mode = 4;
				}
				//or just eat regular pills if there aren't any prey to chase
				else
				{
					mode = 5;
				}

				return -1;
			}

			//if we avoid the above conditions then grab that pill
			Node closestPowerPill = myGuy.getTargetNode(game.getPowerPillList(), true);
			return myGuy.getNextDir(closestPowerPill, true);
		}



		if (mode == 4) //Chase Mode
		{

			//no prey? go back to chasing power pills
			if (prey.size() == 0)
			{
				mode = 1;
				return -1;
			}

			//if we got some predators in the maze while chasing prey..
			if(predators.size() > 0)
			{
				//track all the nodes from myGuy to its nearest prey
				List<Node> pathToPrey = myGuy.getPathTo(myGuy.getTargetNode(prey, true));

				//and loop through each of those nodes
				for (Node checkNode : pathToPrey)
				{
					//while looping through each of the predators out and about
					for (Node defender : predators)
					{
						//and check if any predators are found in your path of nodes
						if (checkNode.getX() == defender.getX() && checkNode.getY() == defender.getY())
						{
							//if so, run away from that predator!
							return myGuy.getNextDir(myGuy.getTargetNode(predators, true), false);
						}
					}
				}
			}

			//otherwise just chase that prey
			return myGuy.getNextDir(myGuy.getTargetNode(prey, true), true);
		}

		//since this isn't in a "mode" it will be triggered by any number other than 1-4
		//and it means just get the normal pills
		return myGuy.getNextDir(myGuy.getTargetNode(game.getPillList(), true), true);
	}
}