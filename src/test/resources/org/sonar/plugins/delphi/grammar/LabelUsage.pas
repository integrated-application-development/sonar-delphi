unit LabelUsage;

interface

implementation

function ParseBuffer: Integer;
label
  redo;
begin
  repeat
	  redo : Inc(Result);

	  goto redo;
  until true;

  redo: Inc(Result);
end;


end.
