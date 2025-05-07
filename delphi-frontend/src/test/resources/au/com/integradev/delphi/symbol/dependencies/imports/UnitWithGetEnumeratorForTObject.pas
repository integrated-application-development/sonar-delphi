unit UnitWithGetEnumeratorForTObject;

interface

type
  TObjectHelper = class helper for TObject
    function GetEnumerator: TObject;
    function MoveNext: Boolean;
    property Current: TObject;
  end;

implementation

end.