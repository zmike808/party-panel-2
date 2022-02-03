/*
 * Copyright (c) 2020, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.zmike808.runelite.partypanel;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import com.zmike808.runelite.partypanel.data.PartyPlayer;
import com.zmike808.runelite.partypanel.ui.PlayerBanner;
import com.zmike808.runelite.partypanel.ui.PlayerPanel;

class PartyPanel extends PluginPanel
{
	private static final Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private static final Color BACKGROUND_HOVER_COLOR = ColorScheme.DARK_GRAY_HOVER_COLOR;

	private final PartyPanelPlugin plugin;
	private final Map<UUID, PlayerBanner> bannerMap = new HashMap<>();
	private final JPanel panel;
	private PlayerPanel playerPanel = null;
	private PartyPlayer selectedPlayer = null;

	@Inject
	PartyPanel(final PartyPanelPlugin plugin)
	{
		super(false);
		this.plugin = plugin;
		this.setLayout(new BorderLayout());

		panel = new JPanel();
		panel.setBorder(new EmptyBorder(BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET, BORDER_OFFSET));
		panel.setLayout(new DynamicGridLayout(0, 1, 0, 3));

		// Wrap content to anchor to top and prevent expansion
		final JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(panel, BorderLayout.NORTH);
		final JScrollPane scrollPane = new JScrollPane(northPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		this.add(scrollPane, BorderLayout.CENTER);
		this.add(createLeaveButton(), BorderLayout.SOUTH);
	}

	void refreshUI()
	{
		panel.removeAll();
		if (selectedPlayer == null)
		{
			showBannerView();
		}
		else if (plugin.getPartyMembers().containsKey(selectedPlayer.getMemberId()))
		{
			showPlayerView();
		}
		else
		{
			selectedPlayer = null;
			showBannerView();
		}
	}

	/**
	 * Shows all members of the party, excluding the local player, in banner view. See {@link PlayerBanner)
	 */
	void showBannerView()
	{
		selectedPlayer = null;
		panel.removeAll();

		final Collection<PartyPlayer> players = plugin.getPartyMembers().values()
			.stream()
			// Sort by username, if it doesn't exist use their discord name
			.sorted(Comparator.comparing(o -> o.getUsername() == null ? o.getMember().getName() : o.getUsername()))
			.collect(Collectors.toList());

		for (final PartyPlayer player : players)
		{
			final PlayerBanner banner = new PlayerBanner(player, plugin.spriteManager);
			banner.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					if (e.getButton() == MouseEvent.BUTTON1)
					{
						selectedPlayer = player;
						showPlayerView();
					}
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					banner.setBackground(BACKGROUND_HOVER_COLOR);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					banner.setBackground(BACKGROUND_COLOR);
				}
			});
			panel.add(banner);
			bannerMap.put(player.getMember().getMemberId(), banner);
		}

		if (getComponentCount() == 0)
		{
			panel.add(new JLabel("There are no members in your party"));
		}

		panel.revalidate();
		panel.repaint();
	}

	void showPlayerView()
	{
		if (selectedPlayer == null)
		{
			showBannerView();
		}

		panel.removeAll();
		panel.add(createReturnButton());

		if (playerPanel != null)
		{
			playerPanel.changePlayer(selectedPlayer);
		}
		else
		{
			playerPanel = new PlayerPanel(selectedPlayer, plugin.spriteManager, plugin.itemManager);
		}
		panel.add(playerPanel);

		panel.revalidate();
		panel.repaint();
	}

	private JButton createReturnButton()
	{
		final JButton label = new JButton("Return to party overview");
		label.setFocusable(false);
		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				label.setBackground(BACKGROUND_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				label.setBackground(BACKGROUND_COLOR);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					showBannerView();
				}
			}
		});

		return label;
	}

	private JButton createLeaveButton()
	{
		final JButton label = new JButton("Leave Party");
		label.setFocusable(false);
		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				label.setBackground(BACKGROUND_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				label.setBackground(BACKGROUND_COLOR);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					selectedPlayer = null;
					bannerMap.clear();
					playerPanel = null;
					plugin.leaveParty();
				}
			}
		});

		return label;
	}

	void updatePartyPlayer(final PartyPlayer player)
	{
		if (selectedPlayer == null)
		{
			final PlayerBanner panel = bannerMap.get(player.getMemberId());
			if (panel == null)
			{
				// New member, recreate entire view
				showBannerView();
				return;
			}

			final String oldPlayerName = panel.getPlayer().getUsername();
			panel.setPlayer(player);
			if (!Objects.equals(player.getUsername(), oldPlayerName))
			{
				panel.recreatePanel();
			}
			else
			{
				panel.refreshStats();
			}
		}
		else
		{
			if (player.getMemberId().equals(selectedPlayer.getMemberId()))
			{
				this.selectedPlayer = player;
				showPlayerView();
			}
		}
	}

	void removePartyPlayer(final PartyPlayer player)
	{
		bannerMap.remove(player.getMemberId());

		if (selectedPlayer != null && !selectedPlayer.getMemberId().equals(player.getMemberId()))
		{
			return;
		}

		selectedPlayer = null;
		showBannerView();
	}
}
