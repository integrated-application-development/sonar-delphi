unit UnitWithGetEnumeratorForTObject;

interface

type
  TObjectHelper = class helper for TObject
    function GetEnumerator: TObject;
  end;

implementation

end.