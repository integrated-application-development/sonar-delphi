unit CyclomaticComplexityTest;

interface

uses
  Windows;

type
  TCyclomaticComplexityTest = class(TForm)
  public
  procedure fooZZ;
  end;
var
  i : Integer;

implementation
  // Cyclomatic Complexity = E - N + 2*P,  where
  // E = number of edges
  // N = number of nodes
  // P = nodes that have exit end points
  // This procedure below has a cyclomatic complexity of 31
procedure  TCyclomaticComplexityTest.fooZZ;
begin
  i := 30;
  if i=1 then ShowMessage('its 1') else exit;
  if i=2 then ShowMessage('its 2') else exit;
  if i=3 then ShowMessage('its 3') else exit;
  if i=4 then ShowMessage('its 4') else exit;
  if i=5 then ShowMessage('its 5') else exit;
  if i=6 then ShowMessage('its 6') else exit;
  if i=7 then ShowMessage('its 7') else exit;
  if i=8 then ShowMessage('its 8') else exit;
  if i=9 then ShowMessage('its 9') else exit;
  if i=10 then ShowMessage('its 10') else exit;
  if i=11 then ShowMessage('its 11') else exit;
  if i=12 then ShowMessage('its 12') else exit;
  if i=13 then ShowMessage('its 13') else exit;
  if i=14 then ShowMessage('its 14') else exit;
  if i=15 then ShowMessage('its 15') else exit;
  if i=16 then ShowMessage('its 16') else exit;
  if i=17 then ShowMessage('its 17') else exit;
  if i=18 then ShowMessage('its 18') else exit;
  if i=19 then ShowMessage('its 19') else exit;
  if i=20 then ShowMessage('its 20') else exit;
  if i=21 then ShowMessage('its 21') else exit;
  if i=22 then ShowMessage('its 22') else exit;
  if i=23 then ShowMessage('its 23') else exit;
  if i=24 then ShowMessage('its 24') else exit;
  if i=25 then ShowMessage('its 25') else exit;
  if i=26 then ShowMessage('its 26') else exit;
  if i=27 then ShowMessage('its 27') else exit;
  if i=28 then ShowMessage('its 28') else exit;
  if i=29 then ShowMessage('its 29') else exit;
  if i=30 then ShowMessage('its 30') else exit;
end;

end.